package com.saathisquare.societyservice.service;

import com.saathisquare.societyservice.dto.request.AssignUserRequest;
import com.saathisquare.societyservice.dto.request.CreateFlatRequest;
import com.saathisquare.societyservice.dto.response.FlatResponse;
import com.saathisquare.societyservice.util.Response;

public interface FlatService {

	FlatResponse createFlat(CreateFlatRequest request);

	Response<String> assignUserToFlat(AssignUserRequest request);

}
