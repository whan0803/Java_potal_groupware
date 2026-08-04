package user.service;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.dto.DuplicateCheckResponse;
import user.dto.UserCreateRequest;
import user.dto.UserResponse;
import user.dto.UserRoleUpdateRequest;
import user.dto.UserUpdateRequest;
import user.entity.Role;
import user.entity.User;
import user.entity.UserRole;
import user.repository.RoleRepository;
import user.repository.UserRepository;
import user.repository.UserRoleRepository;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 목록 조회, 검색, 페이징
    public Page<UserResponse> getUsers(
            String loginId,
            String userName,
            String email,
            String useYn,
            Long roleId,
            Pageable pageable
    ) {
        Specification<User> specification =
                ((root, query, cb) -> cb.conjunction());

        if (loginId != null && !loginId.isBlank()) {
            String keyword = loginId.trim().toLowerCase();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("loginId")),
                                    "%" + keyword + "%"
                            )
            );
        }

        if (userName != null && !userName.isBlank()) {
            String keyword = userName.trim().toLowerCase();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("userName")),
                                    "%" + keyword + "%"
                            )
            );
        }

        if (email != null && !email.isBlank()) {
            String keyword = email.trim().toLowerCase();

            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("email")),
                                    "%" + keyword + "%"
                            )
            );
        }

        if (useYn != null && !useYn.isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.equal(
                                    cb.upper(root.get("useYn")),
                                    useYn.trim().toUpperCase()
                            )
            );
        }

        if (roleId != null) {
            specification = specification.and(
                    (root, query, cb) -> {
                        query.distinct(true);

                        return cb.equal(
                                root.join("userRoles", JoinType.INNER)
                                        .join("role", JoinType.INNER)
                                        .get("roleId"),
                                roleId
                        );
                    }
            );
        }

        return userRepository.findAll(specification, pageable)
                .map(UserResponse::from);
    }

    // 사용자 상세 조회
    public UserResponse getUser(Long userId) {
        User user = userRepository.findDetailByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        return UserResponse.from(user);
    }

    // 로그인 아이디 중복 확인
    public DuplicateCheckResponse checkLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException(
                    "로그인 아이디를 입력해 주세요."
            );
        }

        boolean duplicated =
                userRepository.existsByLoginId(loginId.trim());

        return new DuplicateCheckResponse(
                !duplicated,
                duplicated
                        ? "이미 사용 중인 아이디입니다."
                        : "사용 가능한 아이디입니다."
        );
    }

    // 이메일 중복 확인
    public DuplicateCheckResponse checkEmail(
            String email,
            Long userId
    ) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "이메일을 입력해 주세요."
            );
        }

        String normalizedEmail = email.trim();
        boolean duplicated;

        if (userId == null) {
            duplicated =
                    userRepository.existsByEmail(normalizedEmail);
        } else {
            duplicated =
                    userRepository.existsByEmailAndUserIdNot(
                            normalizedEmail,
                            userId
                    );
        }

        return new DuplicateCheckResponse(
                !duplicated,
                duplicated
                        ? "이미 사용 중인 이메일입니다."
                        : "사용 가능한 이메일입니다."
        );
    }

    // 사용자 생성
    @Transactional
    public Long createUser(
            UserCreateRequest request,
            Long actorId
    ) {
        validateCreateRequest(request);

        User user = new User(
                request.getLoginId().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getUserName().trim(),
                normalizeNullable(request.getEmail()),
                normalizeNullable(request.getPhone()),
                request.getUseYn(),
                actorId
        );

        User savedUser = userRepository.save(user);

        updateUserRoles(
                savedUser,
                request.getRoleIds(),
                actorId
        );

        return savedUser.getUserId();
    }

    // 사용자 정보 수정
    @Transactional
    public void updateUser(
            Long userId,
            UserUpdateRequest request,
            Long actorId
    ) {
        User user = findUser(userId);

        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && userRepository.existsByEmailAndUserIdNot(
                request.getEmail().trim(),
                userId
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        user.update(
                normalizeNullable(request.getUserName()),
                normalizeNullable(request.getEmail()),
                normalizeNullable(request.getPhone()),
                request.getUseYn(),
                actorId
        );

        if (request.getRoleIds() != null) {
            updateUserRoles(
                    user,
                    request.getRoleIds(),
                    actorId
            );
        }
    }

    // 사용자 사용 중지
    @Transactional
    public void deactivateUser(
            Long userId,
            Long actorId
    ) {
        User user = findUser(userId);

        if ("N".equals(user.getUseYn())) {
            throw new IllegalStateException(
                    "이미 사용 중지된 사용자입니다."
            );
        }

        user.deactivate(actorId);
    }

    // 사용자 권한 수정
    @Transactional
    public void updateRoles(
            Long userId,
            UserRoleUpdateRequest request,
            Long actorId
    ) {
        User user = findUser(userId);

        updateUserRoles(
                user,
                request.getRoleIds(),
                actorId
        );
    }

    // 비밀번호 초기화
    @Transactional
    public String resetPassword(
            Long userId,
            Long actorId
    ) {
        User user = findUser(userId);

        String temporaryPassword = createTemporaryPassword();

        user.changePassword(
                passwordEncoder.encode(temporaryPassword),
                actorId
        );

        return temporaryPassword;
    }

    // 사용자 조회 공통 메서드
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }

    // 사용자 등록 시 중복 검증
    private void validateCreateRequest(
            UserCreateRequest request
    ) {
        if (userRepository.existsByLoginId(
                request.getLoginId().trim()
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다."
            );
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && userRepository.existsByEmail(
                request.getEmail().trim()
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }
    }

    // 사용자 권한 변경
    private void updateUserRoles(
            User user,
            List<Long> roleIds,
            Long actorId
    ) {
        if (roleIds == null) {
            return;
        }

        Set<Long> uniqueRoleIds = new HashSet<>(roleIds);

        if (uniqueRoleIds.size() != roleIds.size()) {
            throw new IllegalArgumentException(
                    "중복된 권한이 포함되어 있습니다."
            );
        }

        List<Role> roles = roleRepository.findAllById(uniqueRoleIds);

        if (roles.size() != uniqueRoleIds.size()) {
            throw new IllegalArgumentException(
                    "존재하지 않는 권한이 포함되어 있습니다."
            );
        }

        boolean containsInactiveRole = roles.stream()
                .anyMatch(role -> !"Y".equals(role.getUseYn()));

        if (containsInactiveRole) {
            throw new IllegalArgumentException(
                    "사용 중지된 권한은 지정할 수 없습니다."
            );
        }

        /*
         * 검증이 끝난 후 기존 권한을 삭제해야 합니다.
         * 검증 전에 삭제하면 잘못된 roleId가 들어왔을 때
         * 기존 권한이 먼저 제거될 수 있습니다.
         */
        userRoleRepository.deleteAllByUserUserId(
                user.getUserId()
        );

        if (roles.isEmpty()) {
            return;
        }

        List<UserRole> userRoles = roles.stream()
                .map(role ->
                        new UserRole(
                                user,
                                role,
                                actorId
                        )
                )
                .toList();

        userRoleRepository.saveAll(userRoles);
    }

    // 임시 비밀번호 생성
    private String createTemporaryPassword() {
        String characters =
                "ABCDEFGHJKLMNPQRSTUVWXYZ"
                        + "abcdefghijkmnopqrstuvwxyz"
                        + "23456789"
                        + "!@#";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }

    // 공백 문자열을 null로 변환
    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
