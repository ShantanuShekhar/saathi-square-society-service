package com.saathisquare.societyservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocietyDataRequest {
        private String createdBy;
        private String societyId;
        private String towerId;
        private String flatNo;
        private Integer floorNo;
        private String status;
          private int pageNo;
        private int pageSize;
}
