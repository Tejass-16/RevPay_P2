package com.revpay.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpay.dto.RegisterRequest;
import com.revpay.entity.User;
import com.revpay.entity.Wallet;
import com.revpay.repository.UserRepository;
import com.revpay.repository.WalletRepository;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest registerRequest) {
        logger.info("Registering new user with email: {}", registerRequest.getEmail());

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User.UserRole role;
        if (registerRequest.getRole().equalsIgnoreCase("USER")) {
            role = User.UserRole.PERSONAL;
        } else {
            role = User.UserRole.valueOf(registerRequest.getRole().toUpperCase());
        }
        
        User user = new User();
        user.setFullName(registerRequest.getFirstName() + " " + registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone("AUTO" + System.currentTimeMillis()); // Generate unique phone
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setTransactionPin(registerRequest.getTransactionPin());
        user.setUserRole(role);
        user.setActive(true);
        user.setVerified(false);

        if (role == User.UserRole.BUSINESS) {
            user.setBusinessName(registerRequest.getBusinessName());
            user.setBusinessType(registerRequest.getBusinessType());
            user.setTaxId(registerRequest.getTaxId());
            user.setBusinessAddress(registerRequest.getBusinessAddress());
        } else {
            // Set default values for PERSONAL users
            user.setBusinessName(null);
            user.setBusinessType(null);
            user.setTaxId(null);
            user.setBusinessAddress(null);
        }
        user.setVerificationStatus(User.VerificationStatus.PENDING);

        user = userRepository.save(user);

        Wallet wallet = new Wallet(user);
        walletRepository.save(wallet);

        logger.info("User registered successfully with ID: {}", user.getId());
        return user;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User updateProfile(Long userId, String fullName, String phone) {
        User user = findById(userId);
        
        if (!user.getPhone().equals(phone) && userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        user.setFullName(fullName);
        user.setPhone(phone);
        
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        logger.info("Password changed successfully for user ID: {}", userId);
    }

    public void changeTransactionPin(Long userId, String currentPin, String newPin) {
        User user = findById(userId);
        
        if (!currentPin.equals(user.getTransactionPin())) {
            throw new IllegalArgumentException("Current transaction PIN is incorrect");
        }

        user.setTransactionPin(newPin);
        userRepository.save(user);
        
        logger.info("Transaction PIN changed successfully for user ID: {}", userId);
    }

    public void deactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(false);
        userRepository.save(user);
        
        logger.info("User deactivated with ID: {}", userId);
    }

    public void activateUser(Long userId) {
        User user = findById(userId);
        user.setActive(true);
        userRepository.save(user);
        
        logger.info("User activated with ID: {}", userId);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean verifyTransactionPin(Long userId, String pin) {
        User user = findById(userId);
        return pin.equals(user.getTransactionPin());
    }
}
