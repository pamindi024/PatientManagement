package com.medicalsystem.patientmanagement.service;

import com.medicalsystem.patientmanagement.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PatientService patientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Patient patient = patientService.findByUsername(username);
            if (patient == null) {
                throw new UsernameNotFoundException("User not found");
            }

            // If the password is not BCrypt encoded, encode it
            String password = patient.getPassword();
            if (!password.startsWith("$2a$")) {
                password = passwordEncoder.encode(password);
                // Update the patient's password in the file
                patient.setPassword(password);
                patientService.updatePatient(patient);
            }

            return new User(
                patient.getUsername(),
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found", e);
        }
    }
} 