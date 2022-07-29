package com.madama.data.generator;

import com.madama.data.Role;
import com.madama.data.entity.Project;
import com.madama.data.entity.Technologie;
import com.madama.data.entity.User;
import com.madama.data.service.ProjectRepository;
import com.madama.data.service.TechnologieRepository;
import com.madama.data.service.UserRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
            ProjectRepository projectRepository, TechnologieRepository technologieRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 2 User entities...");
            User user = new User();
            user.setName("John Normal");
            user.setUsername("user");
            user.setHashedPassword(passwordEncoder.encode("user"));
            user.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);
            User admin = new User();
            admin.setName("Emma Powerful");
            admin.setUsername("admin");
            admin.setHashedPassword(passwordEncoder.encode("admin"));
            admin.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            admin.setRoles(Set.of(Role.USER, Role.ADMIN));
            userRepository.save(admin);
            logger.info("... generating 100 Project entities...");
            ExampleDataGenerator<Project> projectRepositoryGenerator = new ExampleDataGenerator<>(Project.class,
                    LocalDateTime.of(2022, 7, 29, 0, 0, 0));
            projectRepositoryGenerator.setData(Project::setName, DataType.FIRST_NAME);
            projectRepositoryGenerator.setData(Project::setVersion, DataType.WORD);
            projectRepositoryGenerator.setData(Project::setPhase, DataType.WORD);
            projectRepositoryGenerator.setData(Project::setMethodo, DataType.WORD);
            projectRepositoryGenerator.setData(Project::setClient, DataType.WORD);
            projectRepository.saveAll(projectRepositoryGenerator.create(100, seed));

            logger.info("... generating 100 Technologie entities...");
            ExampleDataGenerator<Technologie> technologieRepositoryGenerator = new ExampleDataGenerator<>(
                    Technologie.class, LocalDateTime.of(2022, 7, 29, 0, 0, 0));
            technologieRepositoryGenerator.setData(Technologie::setName, DataType.WORD);
            technologieRepositoryGenerator.setData(Technologie::setVersion, DataType.WORD);
            technologieRepositoryGenerator.setData(Technologie::setIsLts, DataType.BOOLEAN_90_10);
            technologieRepository.saveAll(technologieRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}