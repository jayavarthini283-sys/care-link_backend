package com.example.demo.service;

import com.example.demo.dto.BookingRequestDto;
import com.example.demo.entity.Account;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.AvailabilitySlot;
import com.example.demo.entity.DoctorProfile;
import com.example.demo.entity.PatientProfile;
import com.example.demo.exception.AppointmentLimitExceededException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.AvailabilitySlotRepository;
import com.example.demo.repository.DoctorProfileRepository;
import com.example.demo.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AccountRepository accountRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    private static final int MAX_PENDING_APPOINTMENTS = 3;

    @Transactional
    public Appointment bookAppointment(String email, BookingRequestDto dto) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        PatientProfile patient = patientProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for: " + email));

        long pendingCount = appointmentRepository.countByPatientIdAndStatus(
                patient.getId(), Appointment.AppointmentStatus.PENDING);

        if (pendingCount >= MAX_PENDING_APPOINTMENTS) {
            throw new AppointmentLimitExceededException(
                    "Maximum of 3 pending appointments allowed. Please complete or cancel existing ones.");
        }

        AvailabilitySlot slot = availabilitySlotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + dto.getSlotId()));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot is already booked.");
        }

        slot.setBooked(true);
        availabilitySlotRepository.save(slot);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(slot.getDoctor())
                .slot(slot)
                .status(Appointment.AppointmentStatus.PENDING)
                .reasonForVisit(dto.getReasonForVisit())
                .build();

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelAppointment(String email, Long appointmentId) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        boolean isOwner = appointment.getPatient().getAccount().getId().equals(account.getId());
        boolean isDoctor = appointment.getDoctor().getAccount().getId().equals(account.getId());

        if (!isOwner && !isDoctor) {
            throw new RuntimeException("Unauthorized to cancel this appointment.");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED
                || appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel an appointment that is already cancelled or completed.");
        }

        AvailabilitySlot slot = appointment.getSlot();
        slot.setBooked(false);
        availabilitySlotRepository.save(slot);

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public List<Appointment> getPatientAppointments(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));

        if (account.getRole() == Account.Role.DOCTOR) {
            DoctorProfile doctorProfile = doctorProfileRepository.findByAccountId(account.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for: " + email));
            return appointmentRepository.findByDoctorId(doctorProfile.getId());
        }

        PatientProfile patientProfile = patientProfileRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for: " + email));
        return appointmentRepository.findByPatientId(patientProfile.getId());
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
}
