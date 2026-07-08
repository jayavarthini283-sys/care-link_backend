package com.example.demo.service;

import com.example.demo.entity.PatientProfile;
import com.example.demo.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientProfileRepository patientProfileRepository;

    public List<PatientProfile> getAllPatients() {
        return patientProfileRepository.findAll();
    }

    public void deletePatient(Long id) {
        patientProfileRepository.deleteById(id);
    }
}
