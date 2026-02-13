package com.saathisquare.societyservice.service;

import com.saathisquare.societyservice.client.RbacClient;
import com.saathisquare.societyservice.dto.response.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.saathisquare.societyservice.util.Response;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for batch user lookups to prevent N+1 query problems
 * Caches user names to avoid repeated Feign client calls
 */
@Service
@RequiredArgsConstructor
public class BatchUserLookupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUserLookupService.class);
    private final RbacClient rbacClient;
    
    // In-memory cache for user names (simple cache, can be replaced with Redis in production)
    private final Map<UUID, String> userNameCache = new ConcurrentHashMap<>();
    
    /**
     * Get user name by ID (with caching)
     * @param userId User ID
     * @return User name or default fallback
     */
    public String getUserName(UUID userId) {
        if (userId == null) {
            return "Unknown";
        }

        // Check cache first
        String cachedName = userNameCache.get(userId);
        if (cachedName != null) {
            return cachedName;
        }

        // Fetch from RBAC service
        try {
            ResponseEntity<Response<UserDetailsResponse>> userResponse = 
                    rbacClient.getLoginDetailsByUsername(userId.toString());
            
            if (userResponse.getBody() != null && userResponse.getBody().getData() != null) {
                UserDetailsResponse user = userResponse.getBody().getData();
                String userName = user.getFullName() != null && !user.getFullName().isEmpty()
                        ? user.getFullName()
                        : user.getUsername();
                
                // Cache the result
                userNameCache.put(userId, userName);
                return userName;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch user name for userId: {}, error: {}", userId, e.getMessage());
        }

        // Return default if fetch fails
        String defaultName = "Resident";
        userNameCache.put(userId, defaultName); // Cache default to avoid repeated failures
        return defaultName;
    }

    /**
     * Batch lookup user names for multiple user IDs
     * Uses parallel processing to fetch user names concurrently
     * 
     * @param userIds Set of user IDs to lookup
     * @return Map of userId -> userName
     */
    public Map<UUID, String> getUserNames(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, String> result = new HashMap<>();
        Set<UUID> uncachedIds = new HashSet<>();

        // First, check cache
        for (UUID userId : userIds) {
            String cachedName = userNameCache.get(userId);
            if (cachedName != null) {
                result.put(userId, cachedName);
            } else {
                uncachedIds.add(userId);
            }
        }

        // If all were cached, return early
        if (uncachedIds.isEmpty()) {
            return result;
        }

        String correlationId = MDC.get("correlationId");
        LOGGER.debug("[{}] Batch fetching {} user names ({} already cached)", 
                    correlationId, uncachedIds.size(), result.size());

        // Fetch uncached user names in parallel
        List<CompletableFuture<AbstractMap.SimpleEntry<UUID, String>>> futures = uncachedIds.stream()
                .map(userId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ResponseEntity<Response<UserDetailsResponse>> userResponse = 
                                rbacClient.getLoginDetailsByUsername(userId.toString());
                        
                        if (userResponse.getBody() != null && userResponse.getBody().getData() != null) {
                            UserDetailsResponse user = userResponse.getBody().getData();
                            String userName = user.getFullName() != null && !user.getFullName().isEmpty()
                                    ? user.getFullName()
                                    : user.getUsername();
                            
                            // Cache the result
                            userNameCache.put(userId, userName);
                            return new AbstractMap.SimpleEntry<>(userId, userName);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to fetch user name for userId: {}, error: {}", userId, e.getMessage());
                    }
                    
                    // Default fallback
                    String defaultName = "Resident";
                    userNameCache.put(userId, defaultName);
                    return new AbstractMap.SimpleEntry<>(userId, defaultName);
                }))
                .collect(Collectors.toList());

        // Wait for all futures and collect results
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<AbstractMap.SimpleEntry<UUID, String>> future : futures) {
            try {
                AbstractMap.SimpleEntry<UUID, String> entry = future.get();
                result.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                LOGGER.warn("Error getting user name from future: {}", e.getMessage());
            }
        }

        LOGGER.debug("[{}] Batch fetch complete. Retrieved {} user names", correlationId, result.size());
        return result;
    }

    /**
     * Invalidate cache entry for a user
     * @param userId User ID to invalidate
     */
    public void invalidateCache(UUID userId) {
        if (userId != null) {
            userNameCache.remove(userId);
        }
    }

    /**
     * Clear entire cache (useful for testing or cache refresh)
     */
    public void clearCache() {
        userNameCache.clear();
    }

    /**
     * Get cache size (useful for monitoring)
     * @return Number of cached entries
     */
    public int getCacheSize() {
        return userNameCache.size();
    }
}

