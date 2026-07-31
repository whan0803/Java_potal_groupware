package user.service;

import user.dto.*;
import user.entity.Role;
import user.entity.User;
import user.entity.UserRole;
import user.repository.RoleRepository;
import user.repository.UserRoleRepository;
import user.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> getUsers(
            String loginId,
            String userName,
            String email,
            String useYn,
            Long roleId,
            Pageable pageable
    ) {
        Specification<User> specification = (root, query, cb) -> null;

        if (loginId != null && !loginId.isBlank()) {
            specification = specification.and(
                    ((root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("loginId")),
                                    "%" + loginId.toLowerCase() + "%"
                            ))
            );
        }

        if (userName != null && !userName.isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("userName")),
                                    "%" + userName.toLowerCase() + "%"
                            )
            );

        }
        if (email != null && !email.isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("email")),
                                    "%" + email.toLowerCase() + "%"
                            )
            );



        }
        if (useYn != null && !useYn.isBlank()) {
            specification = specification.and(
                    (root, query, cb) ->
                            cb.like(
                                    cb.lower(root.get("useYn")),
                                    "%" + useYn.toLowerCase() + "%"
                            )
            );
        }

        if(roleId != null) {
            specification = specification.and((root, query, cb) ->{
                query.distinct(true);

                return cb.equal(
                        root.join("userRoles", JoinType.LEFT)
                                .join("role", JoinType.LEFT)
                                .get("roleId"),
                        roleId
                );
            } );
        }

        return userRepository.findAll(specification, pageable)
                .map(UserResponse::from);



    }

    public UserResponse getUser(Long userId) {
        User user = findUser(userId);
        return UserResponse.from(user);
    }

    //아이디 중복 확인
    public DuplicateCheckResponse checkLoginId(String loginId) {
        boolean duplicated = userRepository.existsByLoginId(loginId);

        return new DuplicateCheckResponse(
                !duplicated,
                duplicated ? "이미 사용중인 아이디 입니다" : "사용 가능한 아이디 입니다"
        );
    }


    //이메일 중복 확인
    public DuplicateCheckResponse checkEmail(String email, Long userId) {
        boolean duplicated;

        if(userId == null) {
            duplicated = userRepository.existsByEmail(email);
        }else {
            duplicated = userRepository.existsByEmailAnUserIdNot(email, userId);
        }

        return new DuplicateCheckResponse(
                !duplicated,
                duplicated ? "이미 사용중인 아이디 입니다" : "사용 가능한 아이디 입니다"
        );
    }

    //사용자 생성
    @Transactional
    public Long createUser(UserCreateRequest request, Long actorId) {
        validateCreateRequest(request);

        User user = new User(
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword()),
                request.getUserName(),
                request.getEmail(),
                request.getPhone(),
                request.getUseYn(),
                actorId

        );
        User saveUser = userRepository.save(user);

        updateUserRoles(
                saveUser,
                request.getRolesIds(),
                actorId
        );

        return saveUser.getUser_id();
    }

    @Transactional
    public void deactivateUser(Long userId, Long actorId) {
        User user = findUser(userId);
        user.deactivate(actorId);
    }

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

    @Transactional
    public void updateUser(
            Long userId, UserUpdateRequest request, Long actorId
    ){
        User user = findUser(userId);

        if(request.getEmail() != null && userRepository.existsByEmailAnUserIdNot(request.getEmail(), userId)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일 입니다.");
        }
        user.update(
                request.getUserName(),
                request.getEmail(),
                request.getPhone(),
                request.getUseYn(),
                actorId
        );

        if(request.getRolesIds() != null) {
            updateUserRoles(
                    user,
                    request.getRolesIds(),
                    actorId
            );
        }
    }

    @Transactional
    public String resetPassword(Long userId, Long actorId) {
        User user = findUser(userId);

        String temporaryPassword = createTemporaryPassword();

        user.changePassword(
                passwordEncoder.encode(temporaryPassword),
                actorId
        );

        return temporaryPassword;
    }


    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다"
                        ));
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if(userRepository.existsByLoginId((request.getLoginId()))) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다"
            );
        }

        if(request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "이미 사용중인 이메일 입니다"
            );
        }
    }

    private void updateUserRoles(
            User user,
            List<Long> roleIds,
            Long actorId
    ){
        userRoleRepository.deleteAllByUserUserId((user.getUser_id()));

        if(roleIds == null || roleIds.isEmpty()) {
            return;
        }

        List<Role> roles = roleRepository.findAllById(roleIds);

        if(roles.size() != roleIds.size()) {
            throw new IllegalArgumentException(
                    "존재하지 않는 권한이 포함되어 있습니다"
            );
        }
        List<UserRole> userRoles = roles.stream()
                .map(role -> new UserRole(user, role, actorId))
                .toList();

        userRoleRepository.saveAll(userRoles);
    }

    private String createTemporaryPassword() {
        String characters =
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));

        }
        return password.toString();
    }



}