package com.example.demo.service;

import com.example.demo.entity.DoctorProfile;
import com.example.demo.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;

    public List<DoctorProfile> getAllDoctors() {
        return doctorProfileRepository.findAll();
    }

    public void deleteDoctor(Long id) {
        doctorProfileRepository.deleteById(id);
    }
}
