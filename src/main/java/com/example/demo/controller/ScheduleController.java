package com.example.demo.controller;

import com.example.demo.entity.AvailabilitySlot;
import com.example.demo.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/slots")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> createSlot(@AuthenticationPrincipal User user,
                                            @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime start,
                                            @RequestParam @DateTimeFormat(iso = DATE_TIME) LocalDateTime end) {
        scheduleService.createSlot(user.getUsername(), start, end);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/slots/{doctorId}")
    public ResponseEntity<List<AvailabilitySlot>> getAvailableSlots(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getAvailableSlots(doctorId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AvailabilitySlot>> getMySlots(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(scheduleService.getDoctorSlots(user.getUsername()));
    }
}
