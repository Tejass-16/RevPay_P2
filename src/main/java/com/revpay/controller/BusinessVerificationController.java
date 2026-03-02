package com.revpay.controller;

import com.revpay.entity.User;
import com.revpay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/business-verification")
@CrossOrigin(origins = "http://localhost:4200")
public class BusinessVerificationController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/upload-documents")
    public ResponseEntity<Map<String, Object>> uploadVerificationDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("businessRegistration") MultipartFile businessRegistration,
            @RequestParam("taxDocument") MultipartFile taxDocument,
            @RequestParam("addressProof") MultipartFile addressProof,
            @RequestParam(value = "identityProof", required = false) MultipartFile identityProof) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Check if user has BUSINESS role
        if (!business.getUserRole().equals(User.UserRole.BUSINESS)) {
            throw new RuntimeException("Only business users can upload verification documents");
        }
        
        // Check if already verified
        if (business.getVerificationStatus() == User.VerificationStatus.APPROVED) {
            throw new RuntimeException("Business is already verified");
        }
        
        // In a real implementation, this would:
        // 1. Upload files to cloud storage (S3, etc.)
        // 2. Save file metadata to database
        // 3. Update verification status to PENDING
        // 4. Send notification to admin
        
        // For simulation, we'll just update the status
        business.setVerificationStatus(User.VerificationStatus.PENDING);
        userService.saveUser(business);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Documents uploaded successfully. Your verification is now pending admin approval.");
        response.put("status", business.getVerificationStatus());
        response.put("uploadedFiles", Map.of(
            "businessRegistration", businessRegistration.getOriginalFilename(),
            "taxDocument", taxDocument.getOriginalFilename(),
            "addressProof", addressProof.getOriginalFilename(),
            "identityProof", identityProof != null ? identityProof.getOriginalFilename() : "Not provided"
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("verificationStatus", business.getVerificationStatus());
        response.put("businessName", business.getBusinessName());
        response.put("businessType", business.getBusinessType());
        response.put("taxId", business.getTaxId());
        response.put("businessAddress", business.getBusinessAddress());
        
        // Add additional status information
        switch (business.getVerificationStatus()) {
            case PENDING:
                response.put("message", "Your business verification is pending admin review. This typically takes 2-3 business days.");
                break;
            case APPROVED:
                response.put("message", "Your business is verified! You can now access all business features.");
                break;
            case REJECTED:
                response.put("message", "Your verification was rejected. Please review the feedback and resubmit.");
                break;
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/resubmit")
    public ResponseEntity<Map<String, Object>> resubmitVerification(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> requestData) {
        
        User business = userService.findByEmail(userDetails.getUsername());
        
        if (business == null) {
            throw new RuntimeException("User not found");
        }
        
        // Only allow resubmission if rejected
        if (business.getVerificationStatus() != User.VerificationStatus.REJECTED) {
            throw new RuntimeException("You can only resubmit if your verification was rejected");
        }
        
        // Update business information
        if (requestData.containsKey("businessName")) {
            business.setBusinessName(requestData.get("businessName"));
        }
        if (requestData.containsKey("businessType")) {
            business.setBusinessType(requestData.get("businessType"));
        }
        if (requestData.containsKey("taxId")) {
            business.setTaxId(requestData.get("taxId"));
        }
        if (requestData.containsKey("businessAddress")) {
            business.setBusinessAddress(requestData.get("businessAddress"));
        }
        
        // Reset status to pending
        business.setVerificationStatus(User.VerificationStatus.PENDING);
        userService.saveUser(business);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Verification resubmitted successfully. Your business verification is now pending admin review.");
        response.put("status", business.getVerificationStatus());
        
        return ResponseEntity.ok(response);
    }
    
    // Admin endpoints (would require ADMIN role)
    @GetMapping("/admin/pending-approvals")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingApprovals() {
        // In a real implementation, this would return all pending business verifications
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin endpoint for pending approvals");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/approve/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveBusiness(@PathVariable Long userId) {
        // In a real implementation, this would approve the business verification
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Business approved successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/reject/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectBusiness(
            @PathVariable Long userId,
            @RequestBody Map<String, String> rejectionReason) {
        // In a real implementation, this would reject the business verification with reason
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Business rejected successfully");
        response.put("reason", rejectionReason.get("reason"));
        return ResponseEntity.ok(response);
    }
}
