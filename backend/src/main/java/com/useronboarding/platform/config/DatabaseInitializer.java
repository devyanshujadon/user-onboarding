package com.useronboarding.platform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.useronboarding.platform.model.ERole;
import com.useronboarding.platform.model.Role;
import com.useronboarding.platform.repository.RoleRepository;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles in database...");
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);
            System.out.println("ROLE_USER has been added to database");
        } else {
            System.out.println("Roles already exist in database. Skipping initialization.");
        }
    }
}