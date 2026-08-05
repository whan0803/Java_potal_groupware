package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.backend",
        "auth",
        "boards",
        "common",
        "config",
        "menu",
        "notice",
        "post",
        "security",
        "user"
})
@EntityScan(basePackages = {
        "boards.entity",
        "menu.entity",
        "notice.entity",
        "post.entity",
        "user.entity"
})
@EnableJpaRepositories(basePackages = {
        "boards.repository",
        "menu.repository",
        "notice.repository",
        "post.repository",
        "user.repository"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
