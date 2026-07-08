package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.DoctorProfile;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private static final List<String> PROHIBITED_SUBSTANCES = List.of(
            "fentanyl", "heroin", "methamphetamine", "cocaine"
    );

    private final AccountRepository accountRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public void approveAppointment(String doctorEmail, Long appointmentId) {
        Appointment appointment = resolveOwnedAppointment(doctorEmail, appointmentId);

        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new RuntimeException("Appointment must be PENDING to approve.");
        }
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    public void startConsultation(String doctorEmail, Long appointmentId) {
        Appointment appointment = resolveOwnedAppointment(doctorEmail, appointmentId);

        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Appointment must be CONFIRMED to start.");
        }
        appointment.setStatus(Appointment.AppointmentStatus.IN_PROGRESS);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void finalizeConsultation(String doctorEmail, Long appointmentId, String diagnosis, String medicationsJson) {
        Appointment appointment = resolveOwnedAppointment(doctorEmail, appointmentId);

        String lowerMeds = medicationsJson == null ? "" : medicationsJson.toLowerCase();
        for (String prohibited : PROHIBITED_SUBSTANCES) {
            if (lowerMeds.contains(prohibited)) {
                throw new RuntimeException("Medications contain a prohibited substance: " + prohibited);
            }
        }

        appointment.setDiagnosis(diagnosis);
        appointment.setMedications(medicationsJson);
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
    }

    private Appointment resolveOwnedAppointment(String doctorEmail, Long appointmentId) {
        Account account = accountRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + doctorEmail));

        DoctorProfile doctorProfile = doctorProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for: " + doctorEmail));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorProfile.getId())) {
            throw new RuntimeException("Unauthorized: appointment does not belong to this doctor.");
        }

        return appointment;
    }
}
