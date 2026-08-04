package dashboard.service;


import dashboard.dto.DashboardResponse;
import dashboard.dto.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import security.CustomUserDetails;
import user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;

    public DashboardResponse getDashboard(
            Authentication authentication
    ) {
        CustomUserDetails userDetails =
                getCurrentUser(authentication);

        Long userId = userDetails.getUserId();
        boolean admin = hasAdminRole(authentication);

        Long totalUserCount = null;
        Long activeUserCount = null;

        if(admin) {
            totalUserCount = userRepository.count();
            activeUserCount = userRepository.countByUseYn("Y");
        }

        long pendingApprovalCount = 0L;
        long pendingReservationCount = 0L;
        long inProgressTaskCount = 0L;
        long monthlyScheduleCount = 0L;
        long unreadMessageCount = 0L;

        DashboardSummaryResponse summary =
                new DashboardSummaryResponse(
                        totalUserCount,
                        activeUserCount,
                        pendingApprovalCount,
                        admin ? pendingReservationCount : null,
                        inProgressTaskCount,
                        monthlyScheduleCount,
                        unreadMessageCount
                );

        return new DashboardResponse(
                summary,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private CustomUserDetails getCurrentUser(
            Authentication authentication
    ) {
        if(authentication == null
        ||!authentication.isAuthenticated()
        ||!(authentication.getPrincipal()
                instanceof CustomUserDetails userDetails)){
            throw new IllegalArgumentException(
                    "로그인이 필요합니다"
            );
        }
        return userDetails;
    }

    private boolean hasAdminRole(
            Authentication authentication
    ){
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority())
                        );
    }
}
