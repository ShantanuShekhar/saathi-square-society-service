package com.saathisquare.societyservice.dto;

import java.util.UUID;

public interface FlatInfoDto {
        String getFlatNo();

        String getTowerName();

        Integer getFloor();

        String getOccupancyStatus();

        Double getAreaSqft();

        Double getUnitRate();

        UUID getOwnerId();
}
