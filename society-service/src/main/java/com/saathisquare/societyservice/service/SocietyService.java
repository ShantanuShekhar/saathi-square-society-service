package com.saathisquare.societyservice.service;

import java.util.UUID;

import com.saathisquare.societyservice.dto.FlatInfoDto;
import com.saathisquare.societyservice.dto.request.CreateSocietyRequest;
import com.saathisquare.societyservice.dto.request.SocietyDataRequest;
import com.saathisquare.societyservice.dto.response.PaginatedResponse;
import com.saathisquare.societyservice.dto.response.SocietyResponse;
import com.saathisquare.societyservice.dto.response.UserSocietyDashboardCount;
import com.saathisquare.societyservice.model.Society;
import com.saathisquare.societyservice.util.Response;

public interface SocietyService {

        Response<SocietyResponse> createSociety(CreateSocietyRequest request);

        SocietyResponse getSocietyById(UUID id);

        public Response<PaginatedResponse<Society>> getAllPaginated(SocietyDataRequest request);

        public Response<SocietyResponse> getSocietyDetailsBySocietyId(UUID id, String username);

        public Response<UserSocietyDashboardCount> getSocietyMappingCountByUserId(String id);

        Society getBySocietyId(UUID id);

        Response<PaginatedResponse<FlatInfoDto>> getDetailsPaginated(SocietyDataRequest request);

}
