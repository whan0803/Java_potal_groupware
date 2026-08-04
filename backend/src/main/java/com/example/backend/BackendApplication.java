package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.backend",
        "auth",
        "common",
        "config",
        "menu",
        "security",
        "user"
})
@EntityScan(basePackages = {
        "menu.entity",
        "user.entity"
})
@EnableJpaRepositories(basePackages = {
        "menu.repository",
        "user.repository"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
