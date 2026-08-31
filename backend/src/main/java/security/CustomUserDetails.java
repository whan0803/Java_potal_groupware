package security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import user.entity.User;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String loginId;
    private final String userName;
    private final String password;
    private final String useYn;

    private final Collection<? extends GrantedAuthority> authorities;

    //CustomUserDetailsService가 DB에서 loginId로 사용자 조회
    public CustomUserDetails(
            User user,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.userId = user.getUserId();
        this.loginId = user.getLoginId();
        this.userName = user.getUserName();
        this.password = user.getPassword();
        this.useYn = user.getUseYn();
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "Y".equals(useYn);
    }
}