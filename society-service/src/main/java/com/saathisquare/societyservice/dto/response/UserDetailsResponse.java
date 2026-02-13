package com.saathisquare.societyservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDetailsResponse {

	private UUID id;
	private String username;
	private String email;
	private String fullName;
	private String password;
	private String roleName; // Can include "ROLE_ADMIN", "ROLE_RESIDENT", etc.
}
