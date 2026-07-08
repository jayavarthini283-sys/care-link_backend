package com.example.demo.repository;

import com.example.demo.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByDoctorIdAndBookedFalse(Long doctorId);

    List<AvailabilitySlot> findByDoctorId(Long doctorId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM AvailabilitySlot s " +
           "WHERE s.doctor.id = :doctorId AND s.startTime < :end AND s.endTime > :start")
    boolean existsOverlapping(@Param("doctorId") Long doctorId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);
}
