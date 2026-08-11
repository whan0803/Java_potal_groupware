package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.backend",
        "approval",
        "auth",
        "boards",
        "code",
        "common",
        "config",
        "document",
        "dashboard",
        "file",
        "menu",
        "message",
        "notice",
        "post",
        "reservation",
        "role",
        "schedule",
        "security",
        "task",
        "user"
})
@EntityScan(basePackages = {
        "approval.entity",
        "boards.entity",
        "code.entity",
        "document.entity",
        "file.entity",
        "menu.entity",
        "message.entity",
        "notice.entity",
        "post.entity",
        "reservation.entity",
        "role.entity",
        "schedule.entity",
        "task.entity",
        "user.entity"
})
@EnableJpaRepositories(basePackages = {
        "approval.repository",
        "boards.repository",
        "code.repository",
        "document.repository",
        "file.repository",
        "menu.repository",
        "message.repository",
        "notice.repository",
        "post.repository",
        "reservation.repository",
        "role.repository",
        "schedule.repository",
        "task.repository",
        "user.repository"
})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
