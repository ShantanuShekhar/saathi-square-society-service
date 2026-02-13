package com.saathisquare.societyservice.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.saathisquare.societyservice.dto.request.AssignUserRequest;
import com.saathisquare.societyservice.dto.request.CreateFlatRequest;
import com.saathisquare.societyservice.dto.request.MoveOutRequest;
import com.saathisquare.societyservice.dto.response.FlatResponse;
import com.saathisquare.societyservice.enums.OccupancyStatus;
import com.saathisquare.societyservice.model.Flat;
import com.saathisquare.societyservice.model.UserFlatMapping;
import com.saathisquare.societyservice.repository.FlatRepository;
import com.saathisquare.societyservice.repository.UserFlatMappingRepository;
import com.saathisquare.societyservice.service.FlatService;

import lombok.RequiredArgsConstructor;



/**
 * FlatServiceImpl
 *
 * This service handles operations related to flats and user-flat assignments:
 * - Creating a new flat and marking it as vacant.
 * - Assigning a user to a flat, ensuring only one active assignment per flat.
 * - Moving a user out of a flat (soft delete of the mapping).
 * - Mapping between request/response DTOs and entity models.
 *
 * Dependencies:
 * - FlatRepository: CRUD operations for Flat entities.
 * - UserFlatMappingRepository: Manages user-flat assignment mappings.
 * - ModelMapper: Converts between DTOs and entities.
 *
 * Flow:
 * 1. createFlat: Receives a request, creates a Flat entity, saves it, and returns a response DTO.
 * 2. assignUserToFlat: Checks for existing active assignment, creates a new mapping if none exists.
 * 3. moveOutUser: Finds active mapping, marks it inactive, and sets end date.
 * 4. toEntity/toResponse: Utility methods for mapping between DTOs and entities.
 */


@Service
@RequiredArgsConstructor
public class FlatServiceImpl implements FlatService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlatServiceImpl.class);
	
	private final FlatRepository flatRepository;
	private final UserFlatMappingRepository userMappingRepo;
	private final ModelMapper mapper;

//	public FlatServiceImpl(FlatRepository flatRepository, UserFlatMappingRepository userMappingRepo,
//			ModelMapper mapper) {
//		this.flatRepository = flatRepository;
//		this.userMappingRepo = userMappingRepo;
//		this.mapper = mapper;
//	}

	// Create flat and assign to a society
	@Override
	public FlatResponse createFlat(CreateFlatRequest request) {
		Flat flat = toEntity(request);
		flat.setOccupancyStatus(OccupancyStatus.VACANT);
		flat.setDeleted(false);
		return toResponse(flatRepository.save(flat));
	}

	// Assign user to a flat
	@Override
	public com.saathisquare.societyservice.util.Response<String> assignUserToFlat(AssignUserRequest request) {
		String correlationId = MDC.get("correlationId");
		LOGGER.info("[{}] Attempting to assign user {} to flat {}", correlationId, request.userId(), request.flatId());

		Flat flat = flatRepository.findById(request.flatId())
				.orElseThrow(() -> new com.saathisquare.societyservice.exception.ResourceNotFoundException("Flat not found with id: " + request.flatId()));

		Optional<UserFlatMapping> existing = userMappingRepo.findByFlatFlatIdAndIsActiveTrue(request.flatId());
		if (existing.isPresent()) {
			LOGGER.warn("[{}] Flat {} is already assigned to a user.", correlationId, request.flatId());
			throw new com.saathisquare.societyservice.exception.BusinessException("FLAT_ALREADY_ASSIGNED", "Flat already assigned to a user.");
		}

		UserFlatMapping mapping = UserFlatMapping.builder().userId(request.userId())
				.flat(flat).isActive(true).startDate(LocalDateTime.now())
				.build();

		// Update flat occupancy status
		flat.setOccupancyStatus(OccupancyStatus.OCCUPIED);
		flatRepository.save(flat);
		
		userMappingRepo.save(mapping);
		
		LOGGER.info("[{}] User {} assigned successfully to flat {}. Mapping ID: {}", correlationId, request.userId(), request.flatId(), mapping.getId());
		
		com.saathisquare.societyservice.util.Response<String> response = new com.saathisquare.societyservice.util.Response<>();
		response.setStatus(com.saathisquare.societyservice.util.Constants.SUCCESS_CODE);
		response.setMessage("User assigned successfully.");
		response.setData(mapping.getId().toString());
		return response;
	}

	/**
	 * Soft delete user-flat mapping (move out)
	 */
	public String moveOutUser(MoveOutRequest request) {
		UserFlatMapping mapping = userMappingRepo.findByFlatFlatIdAndIsActiveTrue(request.flatId())
				.orElseThrow(() -> new RuntimeException("No active user found."));
		mapping.setActive(false);
		mapping.setEndDate(LocalDateTime.now());
		userMappingRepo.save(mapping);
		return "User moved out successfully.";
	}

	public Flat toEntity(CreateFlatRequest request) {
		return mapper.map(request, Flat.class);
	}

	public FlatResponse toResponse(Flat flat) {
		return mapper.map(flat, FlatResponse.class);
	}

}
