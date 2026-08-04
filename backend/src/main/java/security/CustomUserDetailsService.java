package security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.entity.User;
import user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "아이디 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        List<SimpleGrantedAuthority> authorities =
                user.getUserRoles()
                        .stream()

                        // 사용 중인 권한만 인증 권한으로 등록
                        .filter(userRole ->
                                "Y".equals(
                                        userRole.getRole().getUseYn()
                                )
                        )

                        .map(userRole ->
                                new SimpleGrantedAuthority(
                                        normalizeRoleCode(
                                                userRole
                                                        .getRole()
                                                        .getRoleCode()
                                        )
                                )
                        )

                        .distinct()
                        .toList();

        return new CustomUserDetails(
                user,
                authorities
        );
    }

    private String normalizeRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalStateException(
                    "권한 코드가 등록되지 않았습니다."
            );
        }

        String normalized =
                roleCode.trim().toUpperCase();

        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }

        return "ROLE_" + normalized;
    }
}