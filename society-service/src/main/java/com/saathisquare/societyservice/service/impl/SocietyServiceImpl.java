package com.saathisquare.societyservice.service.impl;

import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.saathisquare.societyservice.client.RbacClient;
import com.saathisquare.societyservice.dto.FlatInfoDto;
import com.saathisquare.societyservice.dto.request.CreateSocietyRequest;
import com.saathisquare.societyservice.dto.request.SocietyDataRequest;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.dto.response.SocietyResponse;
import com.saathisquare.societyservice.dto.response.UserDetailsResponse;
import com.saathisquare.societyservice.dto.response.UserSocietyDashboardCount;
import com.saathisquare.societyservice.model.Society;
import com.saathisquare.societyservice.repository.SocietyRepository;
import com.saathisquare.societyservice.repository.SocietyUserMappingRepository;
import com.saathisquare.societyservice.service.SocietyService;
import com.saathisquare.societyservice.util.Constants;
import com.saathisquare.societyservice.util.FilterNormalizerUtil;
import com.saathisquare.societyservice.util.Response;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocietyServiceImpl implements SocietyService {
        private static final Logger LOGGER = LoggerFactory.getLogger(SocietyServiceImpl.class);

        private final SocietyRepository societyRepo;
        private final ModelMapper mapper;
        private final RbacClient rbacClient;
        private final SocietyUserMappingRepository societyUserMappingRepository;

        @Override
        public Response<SocietyResponse> createSociety(CreateSocietyRequest request) {

                Response<SocietyResponse> response = new Response<>();

                ResponseEntity<Response<UserDetailsResponse>> extResponse = rbacClient
                                .getLoginDetailsByUsername(String.valueOf(request.createdBy()));

                if (extResponse.getBody().getData() == null) {
                        response.setMessage(extResponse.getBody().getMessage());
                        response.setStatus(extResponse.getBody().getStatus());
                        return response;
                }

                Society society = new Society();
                society.setName(request.name());
                society.setLocation(request.location());
                society.setBillingCycle(request.billingCycle());
                society.setCreatedBy(extResponse.getBody().getData().getId());
                societyRepo.save(society);

                response.setMessage("Society has been " + Constants.CREATED_SUCCESS_MESSAGE);

                response.setData(new SocietyResponse(society.getSocietyId(), society.getName(), society.getLocation(),
                                society.getBillingCycle(), society.getCreatedBy()));
                return response;

        }

        @Override
        public SocietyResponse getSocietyById(UUID id) {
                Society society = societyRepo.findById(id).orElseThrow();
                return new SocietyResponse(society.getSocietyId(), society.getName(), society.getLocation(),
                                society.getBillingCycle(), society.getCreatedBy());
        }

        public List<SocietyResponse> listAll() {
                return societyRepo.findAll().stream().map(society -> toResponse(society)).toList();
        }

        /**
         * Convert a CreateSocietyRequest DTO into a Society entity. ModelMapper matches
         * fields by name. After mapping, you can set any defaults.
         */
        public Society toEntity(CreateSocietyRequest request) {
                return mapper.map(request, Society.class);
        }

        /**
         * Convert a Society entity into a SocietyResponse DTO. Any fields in
         * SocietyResponse with the same name as Society will be copied.
         */
        public SocietyResponse toResponse(Society society) {
                return mapper.map(society, SocietyResponse.class);
        }

        @Override
        public Response<PaginatedResponse<Society>> getAllPaginated(SocietyDataRequest request) {
                Response<PaginatedResponse<Society>> response = new Response<>();
                int pageNo = request.getPageNo();
                int pageSize = request.getPageSize();

                Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

                LOGGER.info("getting created by {} ", request.getCreatedBy());

                Page<Society> pagedSocieties = societyRepo.findAllByCreatedBy(UUID.fromString(request.getCreatedBy()),
                                pageable);
                LOGGER.info("getting pagedSocieties is {} ", pagedSocieties);

                if (!pagedSocieties.hasContent()) {
                        response.setStatus(Constants.VALIDATION_ERROR_API_CODE);
                        response.setMessage(Constants.NO_DATA_FOUND);
                        return response;
                }
                List<Society> societyResponses = pagedSocieties.getContent();

                PaginatedResponse<Society> paginatedResponse = new PaginatedResponse<>(societyResponses,
                                pagedSocieties.getTotalElements(), pagedSocieties.getTotalPages(), pagedSocieties.getNumber(),
                                pagedSocieties.getSize());

                response.setStatus(Constants.SUCCESS_CODE);
                response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
                response.setData(paginatedResponse);
                return response;
        }

        @Override
        public Response<SocietyResponse> getSocietyDetailsBySocietyId(UUID id, String username) {
                Response<SocietyResponse> response = new Response<>();
                ResponseEntity<Response<UserDetailsResponse>> extResponse = rbacClient.getLoginDetailsByUsername(username);

                if (extResponse.getBody().getData() == null) {
                        response.setMessage(extResponse.getBody().getMessage());
                        response.setStatus(extResponse.getBody().getStatus());
                        return response;
                }
                SocietyResponse resp = getSocietyById(id);
                response.setStatus(Constants.SUCCESS_CODE);
                response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
                response.setData(resp);
                return response;
        }

        @Override
        public Response<UserSocietyDashboardCount> getSocietyMappingCountByUserId(String userId) {
                Response<UserSocietyDashboardCount> response = new Response<>();

                LOGGER.info("SocietyServiceImpl :: getSocietyMappingCountByUserId : getting userId : {}", userId);

                UserSocietyDashboardCount data = societyUserMappingRepository.getAllCountsForUser(userId);
                LOGGER.info("getting data is {}", data);
                if (data == null) {
                        response.setStatus(Constants.VALIDATION_ERROR_API_CODE);
                        response.setMessage(Constants.NO_DATA_FOUND);
                } else {
                        response.setStatus(Constants.SUCCESS_CODE);
                        response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
                        response.setData(data);
                }
                return response;
        }

        @Override
        public Society getBySocietyId(UUID id) {
                return societyRepo.findById(id).orElse(null);
        }

        @Override
        public Response<PaginatedResponse<FlatInfoDto>> getDetailsPaginated(SocietyDataRequest request) {
                Response<PaginatedResponse<FlatInfoDto>> response = new Response<>();
                int pageNo = request.getPageNo();
                int pageSize = request.getPageSize();
                Integer floorNo = FilterNormalizerUtil.normalizeInteger(request.getFloorNo());
                String faltNo = FilterNormalizerUtil.normalizeString(request.getFlatNo());
                UUID societyId = FilterNormalizerUtil.normalizeUUID(request.getSocietyId());
                String status = FilterNormalizerUtil.normalizeString(request.getStatus());

                LOGGER.info(
                                "Getting flats with filters: pageNo={}, pageSize={}, floorNo={}, flatNo={}, societyId={}, status={}",
                                pageNo, pageSize, floorNo, faltNo, societyId, status);

                Pageable pageable = PageRequest.of(pageNo - 1, pageSize);

                LOGGER.info("getting created by {} ", request.getCreatedBy());

                Page<FlatInfoDto> pagedSocieties = societyRepo.getFilteredFlatData(pageable, floorNo, faltNo, societyId,
                                status);
                LOGGER.info("getting pagedSocieties is {} ", pagedSocieties);

                if (!pagedSocieties.hasContent()) {
                        response.setStatus(Constants.VALIDATION_ERROR_API_CODE);
                        response.setMessage(Constants.NO_DATA_FOUND);
                        return response;
                }
                List<FlatInfoDto> societyResponses = pagedSocieties.getContent();

                PaginatedResponse<FlatInfoDto> paginatedResponse = new PaginatedResponse<>(societyResponses,
                                pagedSocieties.getTotalElements(), pagedSocieties.getTotalPages(), pagedSocieties.getNumber() + 1,
                                pagedSocieties.getSize());

                response.setStatus(Constants.SUCCESS_CODE);
                response.setMessage(Constants.RETRIVED_SUCCESS_MESSAGE);
                response.setData(paginatedResponse);
                return response;
        }
}
