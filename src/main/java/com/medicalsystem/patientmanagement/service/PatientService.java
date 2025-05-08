package com.medicalsystem.patientmanagement.service;

import com.medicalsystem.patientmanagement.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private static final String FILE_PATH = "patients.txt";

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void updatePatient(Patient updatedPatient) {
        try {
            List<Patient> patients = readPatients();
            for (int i = 0; i < patients.size(); i++) {
                if (patients.get(i).getId().equals(updatedPatient.getId())) {
                    patients.set(i, updatedPatient);
                    break;
                }
            }
            writePatients(patients);
        } catch (IOException e) {
            throw new RuntimeException("Error updating patient", e);
        }
    }

    public Patient findByUsername(String username) {
        try {
            List<Patient> patients = readPatients();
            return patients.stream()
                    .filter(p -> p.getUsername().equals(username))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Error finding patient", e);
        }
    }

    public void registerPatient(Patient patient) {
        try {
            // Generate unique ID
            patient.setId(UUID.randomUUID().toString());

            // Encode password
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));

            // Read existing patients
            List<Patient> patients = readPatients();

            // Add new patient
            patients.add(patient);

            // Write back to file
            writePatients(patients);
        } catch (IOException e) {
            throw new RuntimeException("Error registering patient", e);
        }
    }

    public Patient loginPatient(String username, String password) {
        try {
            List<Patient> patients = readPatients();
            return patients.stream()
                    .filter(p -> p.getUsername().equals(username) &&
                            (passwordEncoder.matches(password, p.getPassword()) ||
                                    password.equals(p.getPassword()))) // Allow both encoded and plain text passwords
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new RuntimeException("Error during login", e);
        }
    }

    public void deletePatient(String id) {
        try {
            List<Patient> patients = readPatients();
            patients.removeIf(p -> p.getId().equals(id));
            writePatients(patients);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting patient", e);
        }
    }

    private List<Patient> readPatients() throws IOException {
        List<Patient> patients = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return patients;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    Patient patient = new Patient(
                            parts[0], // id
                            parts[1], // username
                            parts[2], // password
                            parts[3], // email
                            parts[4], // fullName
                            parts[5], // phoneNumber
                            parts[6]  // address
                    );
                    patients.add(patient);
                }
            }
        }
        return patients;
    }

    private void writePatients(List<Patient> patients) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Patient patient : patients) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s%n",
                        patient.getId(),
                        patient.getUsername(),
                        patient.getPassword(),
                        patient.getEmail(),
                        patient.getFullName(),
                        patient.getPhoneNumber(),
                        patient.getAddress()
                ));
            }
        }
    }
}