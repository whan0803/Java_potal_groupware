package config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import role.repository.RoleRepository;
import user.entity.Role;
import user.entity.User;
import user.entity.UserRole;
import user.repository.UserRepository;
import user.repository.UserRoleRepository;

@Component
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private static final String ADMIN_LOGIN_ID = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_ROLE_CODE = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByRoleCode(ADMIN_ROLE_CODE)
                .orElseGet(() -> roleRepository.save(Role.create(
                        ADMIN_ROLE_CODE,
                        "시스템 관리자",
                        "시스템 전체 관리 권한",
                        "Y",
                        null
                )));

        User admin = userRepository.findByLoginId(ADMIN_LOGIN_ID)
                .orElseGet(() -> userRepository.save(new User(
                        ADMIN_LOGIN_ID,
                        passwordEncoder.encode(ADMIN_PASSWORD),
                        "관리자",
                        null,
                        null,
                        "Y",
                        null
                )));

        admin.setUseYn("Y");
        admin.changePassword(passwordEncoder.encode(ADMIN_PASSWORD), admin.getUserId());

        boolean hasAdminRole = admin.getUserRoles()
                .stream()
                .anyMatch(userRole -> ADMIN_ROLE_CODE.equals(userRole.getRole().getRoleCode()));

        if (!hasAdminRole) {
            userRoleRepository.save(new UserRole(admin, adminRole, admin.getUserId()));
        }
    }
}
