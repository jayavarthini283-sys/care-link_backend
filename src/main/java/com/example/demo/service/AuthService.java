package com.example.demo.service;

import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.RegisterPatientDto;
import com.example.demo.entity.Account;
import com.example.demo.entity.PatientProfile;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
public AuthResponseDto registerPatient(RegisterPatientDto dto) {

    try {

        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        Account account = Account.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Account.Role.PATIENT)
                .active(true)
                .build();

        account = accountRepository.save(account);

        PatientProfile patientProfile = PatientProfile.builder()
                .account(account)
                .fullName(dto.getFullName())
                .bloodGroup(dto.getBloodGroup())
                .emergencyContact(dto.getEmergencyContact())
                .build();

        patientProfileRepository.save(patientProfile);

        UserDetails userDetails = buildUserDetails(account);

        String token = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .email(account.getEmail())
                .role(account.getRole())
                .build();

    } catch (Exception e) {

        e.printStackTrace();

        throw e;
    }
}

    public AuthResponseDto login(LoginRequestDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        Account account = accountRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        UserDetails userDetails = buildUserDetails(account);
        String token = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .token(token)
                .email(account.getEmail())
                .role(account.getRole())
                .build();
    }

    private UserDetails buildUserDetails(Account account) {
        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities("ROLE_" + account.getRole().name())
                .build();
    }
}
