package com.medicalsystem.patientmanagement.controller;

import com.medicalsystem.patientmanagement.model.Patient;
import com.medicalsystem.patientmanagement.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private PatientService patientService;

    @GetMapping
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Patient patient = patientService.findByUsername(auth.getName());
        model.addAttribute("patient", patient);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(Patient updatedPatient, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Patient currentPatient = patientService.findByUsername(auth.getName());

            // Preserve the ID and username
            updatedPatient.setId(currentPatient.getId());
            updatedPatient.setUsername(currentPatient.getUsername());

            // Only update password if a new one is provided
            if (updatedPatient.getPassword() == null || updatedPatient.getPassword().isEmpty()) {
                updatedPatient.setPassword(currentPatient.getPassword());
            } else {
                updatedPatient.setPassword(patientService.encodePassword(updatedPatient.getPassword()));
            }

            patientService.updatePatient(updatedPatient);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/delete")
    public String deleteProfile(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Patient currentPatient = patientService.findByUsername(auth.getName());

            if (currentPatient != null) {
                patientService.deletePatient(currentPatient.getId());
                // Invalidate the session
                SecurityContextHolder.clearContext();
                redirectAttributes.addFlashAttribute("success", "Your account has been successfully deleted.");
                return "redirect:/login?logout";
            }

            redirectAttributes.addFlashAttribute("error", "Error deleting account: User not found");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting account: " + e.getMessage());
            return "redirect:/profile";
        }
    }
}