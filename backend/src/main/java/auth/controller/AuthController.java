package auth.controller;

import auth.dto.CurrentUserResponse;
import auth.dto.LoginRequest;
import auth.dto.LoginResponse;
import auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
            ) {
            LoginResponse response = authService.login(
                    request, httpRequest, httpResponse
            );
            return ResponseEntity.ok(response);
    }
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse>
    getCurrentUser(
            Authentication authentication
    ) {
        CurrentUserResponse response =
                authService.getCurrentUser(authentication);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        authService.logout(
                request,
                response,
                authentication
        );

        return ResponseEntity.ok(
                Map.of("message", "로그아웃되었습니다")
        );
    }
}
