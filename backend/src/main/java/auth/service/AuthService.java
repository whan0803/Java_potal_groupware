package auth.service;

import auth.dto.CurrentUserResponse;
import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import security.CustomUserDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String loginId = request.loginId().trim();

        try{
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            loginId,
                            request.password()
                    );

            Authentication authentication =
                    authenticationManager.authenticate(
                            authenticationToken
                    );

            SecurityContext securityContext =
                    SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(
                    securityContext,
                    httpRequest,
                    httpResponse
            );

            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

            List<String> roles = getRoles(authentication);

            return new LoginResponse(
                    principal.getUserId(),
                    principal.getLoginId(),
                    principal.getUserName(),
                    roles,
                    "로그인 되었습니다"
            );
        }catch (DisabledException e){
            SecurityContextHolder.clearContext();

            throw new ResponseStatusException(
                    FORBIDDEN,
                    "사용이 중지된 계정입니다"
            );
        }catch (AuthenticationException e){
            SecurityContextHolder.clearContext();
            throw new ResponseStatusException(
                    UNAUTHORIZED,
                    "아이디 또는 비밀번호가 올바르지 않습니다"
            );
        }
    }

    public CurrentUserResponse getCurrentUser(
            Authentication authentication
    ){
        CustomUserDetails principal =
                getPrincipal(authentication);

        return new CurrentUserResponse(
                principal.getUserId(),
                principal.getLoginId(),
                principal.getUserName(),
                getRoles(authentication)

        );
    }
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        SecurityContextLogoutHandler logoutHandler =
                new SecurityContextLogoutHandler();

        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);

        logoutHandler.logout(
                request, response, authentication
        );
    }

    private CustomUserDetails getPrincipal(
            Authentication authentication
    ) {
        if(authentication == null || !authentication.isAuthenticated()
            ||!((authentication.getPrincipal()) instanceof  CustomUserDetails principal)
        ){
            throw new IllegalStateException(
                    "로그인 필요합니다"
            );
        }
        return principal;
    }

    private List<String> getRoles(
            Authentication authentication
    ){
        return authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .toList();
    }
}
