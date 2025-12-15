package com.campusevents.config;

import com.campusevents.model.User;
import com.campusevents.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initializes the admin account on application startup.
 * Creates admin@gmail.com with password admin123 if it doesn't exist.
 */
@Component
public class AdminInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";
        
        // Check if admin already exists
        if (!userRepository.existsByEmail(adminEmail)) {
            // Create admin user
            String hashedPassword = passwordEncoder.encode("admin123");
            User admin = new User(
                "Admin",
                "User",
                adminEmail,
                hashedPassword,
                1L // Default to first campus
            );
            admin.setIsAdmin(true); // Set admin flag after creation
            
            userRepository.save(admin);
            System.out.println("Admin account created: admin@gmail.com / admin123");
        } else {
            // Ensure existing admin account has is_admin = true
            userRepository.findByEmail(adminEmail).ifPresent(user -> {
                if (!Boolean.TRUE.equals(user.getIsAdmin())) {
                    // Update to admin if not already
                    user.setIsAdmin(true);
                    // Note: We'd need an update method that includes is_admin
                    // For now, we'll handle this in the migration script
                }
            });
        }
    }
}

