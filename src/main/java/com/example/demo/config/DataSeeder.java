package com.example.demo.config;

import com.example.demo.entity.Account;
import com.example.demo.entity.DoctorProfile;
import com.example.demo.entity.PatientProfile;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.DoctorProfileRepository;
import com.example.demo.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!accountRepository.existsByEmail("admin@carelink.com")) {
            Account admin = Account.builder()
                    .email("admin@carelink.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Account.Role.CLINIC_ADMIN)
                    .active(true)
                    .build();
            accountRepository.save(admin);
        }

        if (!accountRepository.existsByEmail("doctor@carelink.com")) {
            Account doctorAccount = Account.builder()
                    .email("doctor@carelink.com")
                    .password(passwordEncoder.encode("doctor123"))
                    .role(Account.Role.DOCTOR)
                    .active(true)
                    .build();
            doctorAccount = accountRepository.save(doctorAccount);

            DoctorProfile doctorProfile = DoctorProfile.builder()
                    .account(doctorAccount)
                    .specialization("General Medicine")
                    .consultationFee(BigDecimal.valueOf(50))
                    .yearsOfExperience(10)
                    .build();
            doctorProfileRepository.save(doctorProfile);
        }

        if (!accountRepository.existsByEmail("patient@carelink.com")) {
            Account patientAccount = Account.builder()
                    .email("patient@carelink.com")
                    .password(passwordEncoder.encode("patient123"))
                    .role(Account.Role.PATIENT)
                    .active(true)
                    .build();
            patientAccount = accountRepository.save(patientAccount);

            PatientProfile patientProfile = PatientProfile.builder()
                    .account(patientAccount)
                    .fullName("John Doe")
                    .bloodGroup("O+")
                    .emergencyContact("1234567890")
                    .build();
            patientProfileRepository.save(patientProfile);
        }
    }
}
