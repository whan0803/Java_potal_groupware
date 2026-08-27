package security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import role.repository.RoleMenuRepository;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuPermissionService {

    private final RoleMenuRepository roleMenuRepository;

    public void requireRead(Authentication authentication, String... menuUrls) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        Set<String> roleCodes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .flatMap(this::roleCodeAliases)
                .collect(Collectors.toSet());

        if (roleCodes.contains("ADMIN") || roleCodes.contains("ROLE_ADMIN")) {
            return;
        }

        Set<String> urls = Arrays.stream(menuUrls)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toSet());

        if (roleCodes.isEmpty() || urls.isEmpty()
                || !roleMenuRepository.existsReadableMenu(roleCodes, urls)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }
    }

    private Stream<String> roleCodeAliases(String roleCode) {
        String normalized = String.valueOf(roleCode).trim().toUpperCase();
        String withoutPrefix = normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
        return Stream.of(normalized, withoutPrefix, "ROLE_" + withoutPrefix);
    }
}
