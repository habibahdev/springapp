package com.habibah.springapp.initializer;

import com.habibah.springapp.entity.User;
import com.habibah.springapp.entity.enums.Role;
import com.habibah.springapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${APP_ADMIN_EMAIL:}")
    private String email;

    @Value("${APP_ADMIN_PASSWORD:}")
    private String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (email.isBlank() || password.isBlank()) {
            LOGGER.info("initial admin creation is disabled");
            return;
        }
        if (userRepository.existsByEmail(email)) {
            User existUser = userRepository.findByEmail(email).orElseThrow();
            if (existUser.getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                        "The configuration admin email belongs to a non-admin user"
                );
            }
            LOGGER.info("the initial admin account already exists");
            return;
        }

        User admin = User.builder()
                .fullName("Admin Test")
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        LOGGER.info("initial admin account created");
    }
}
