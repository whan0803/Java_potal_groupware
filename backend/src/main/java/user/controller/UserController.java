package user.controller;


import user.dto.*;
import user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserResponse> getUsers(
            @RequestParam(required = false) String loginId,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long roleId,
            Pageable pageable
    ) {
        return userService.getUsers(
                loginId,
                userName,
                email,
                phone,
                roleId,
                pageable
        );
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/check-email")
    public DuplicateCheckResponse checkLoginId(@RequestParam String loginId) {
        return userService.checkLoginId(loginId);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody UserCreateRequest request
            ) {
        Long actorId = null;

        Long userId = userService.createUser(request, actorId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "시용자가 등록되었습니다.",
                        "userId", userId
                ));
    }

    @PutMapping("/{userId}")
    public Map<String, String> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
            ){
        Long actorId = null;

        userService.updateUser(userId, request, actorId);

        return Map.of("message", "사용자 정보가 수정되었습니다");

    }

    @PatchMapping("/{userId}/deactivate")
    public Map<String, String> deactivateUser(
            @PathVariable Long userId
    ) {
        Long actorId = null;

        userService.deactivateUser(userId, actorId);

        return Map.of("message", "사용자가 비활성화 되었습니다");
    }

    @PutMapping("/{userId}/roles")
    public Map<String, String> updateRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
            ){
        Long actorId = null;

        userService.updateRoles(userId, request, actorId);

        return Map.of("message", "사용자 권한이 변경되었습니다");
    }


    @PatchMapping("/{userId}/reset-password")
    public Map<String, String> resetPassword(
            @PathVariable Long userId
    ) {
        Long actorId = null;

        String temporaryPassword =
        userService.resetPassword(userId, actorId);

        return  Map.of(
                "message", "비밀번호가 초기화되었습니다,",
                "temporaryPassword", temporaryPassword
        );


    }



}
