package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.AvailabilitySlot;
import com.example.demo.entity.DoctorProfile;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.AvailabilitySlotRepository;
import com.example.demo.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final AccountRepository accountRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    @Transactional
    public void createSlot(String doctorEmail, LocalDateTime start, LocalDateTime end) {
        Account account = accountRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + doctorEmail));

        DoctorProfile doctorProfile = doctorProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for: " + doctorEmail));

        if (availabilitySlotRepository.existsOverlapping(doctorProfile.getId(), start, end)) {
            throw new RuntimeException("Overlapping slot already exists.");
        }

        AvailabilitySlot slot = AvailabilitySlot.builder()
                .doctor(doctorProfile)
                .startTime(start)
                .endTime(end)
                .booked(false)
                .build();

        availabilitySlotRepository.save(slot);
    }

    public List<AvailabilitySlot> getAvailableSlots(Long doctorId) {
        return availabilitySlotRepository.findByDoctorIdAndBookedFalse(doctorId);
    }

    public List<AvailabilitySlot> getDoctorSlots(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        DoctorProfile doctorProfile = doctorProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for: " + email));

        return availabilitySlotRepository.findByDoctorId(doctorProfile.getId());
    }
}
