package com.saathisquare.societyservice.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saathisquare.societyservice.dto.FlatInfoDto;
import com.saathisquare.societyservice.model.Society;

@Repository
public interface SocietyRepository extends JpaRepository<Society, UUID> {

	Page<Society> findAllByCreatedBy(UUID fromString, Pageable pageable);

	@Query(value = """
            SELECT
                f.flatNumber AS flatNo,
                t.name AS towerName,
                flo.floorNumber AS floor,
                f.occupancyStatus AS occupancyStatus,
                f.areaSqft AS areaSqft,
                f.unitRate AS unitRate,
                ufm.userId AS ownerId
            FROM Flat f
            JOIN f.floor flo
            JOIN flo.tower t
            LEFT JOIN UserFlatMapping ufm ON f.flatId = ufm.flat.id
            WHERE (:floorNo IS NULL OR flo.floorNumber = :floorNo)
            AND (:flatNo IS NULL OR f.flatNumber = :flatNo)
            AND (:societyId IS NULL OR t.society.id = :societyId)
            AND (:status IS NULL OR f.occupancyStatus = :status)
            ORDER BY t.name, flo.floorNumber, f.flatNumber
            """, countQuery = """
            SELECT COUNT(f)
            FROM Flat f
            JOIN f.floor flo
            JOIN flo.tower t
            LEFT JOIN UserFlatMapping ufm ON f.flatId = ufm.flat.id
            WHERE (:floorNo IS NULL OR flo.floorNumber = :floorNo)
            AND (:flatNo IS NULL OR f.flatNumber = :flatNo)
            AND (:societyId IS NULL OR t.society.id = :societyId)
            AND (:status IS NULL OR f.occupancyStatus = :status)
            """)
Page<FlatInfoDto> getFilteredFlatData( Pageable pageable,@Param("floorNo") Integer floorNo, @Param("flatNo") String flatNo,
            @Param("societyId") UUID societyId, @Param("status") String status);
}
