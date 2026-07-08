package com.example.demo.controller;

import com.example.demo.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> approve(@AuthenticationPrincipal User user, @PathVariable Long id) {
        consultationService.approveAppointment(user.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> start(@AuthenticationPrincipal User user, @PathVariable Long id) {
        consultationService.startConsultation(user.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/finalize")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> finalize(@AuthenticationPrincipal User user,
                                          @PathVariable Long id,
                                          @RequestParam String diagnosis,
                                          @RequestParam String medicationsJson) {
        consultationService.finalizeConsultation(user.getUsername(), id, diagnosis, medicationsJson);
        return ResponseEntity.ok().build();
    }
}
