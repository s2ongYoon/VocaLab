package com.dev.vocalab.users.details;

import com.dev.vocalab.users.UsersEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

// Spring Security 인증을 위한 UserDetails(인증용 객체)
// @Data보다는 상속받고있기에 직접 Getter Setter 만드는 것이 낫다.
@Getter
@Setter
public class CustomUsersDetails extends User {

    private String userId;
    private String userName;
    private String nickname;
    private String email;
    private String birthDate;
    private Integer gender;
    private UserSocial userSocial;
    private UserRole userRole;
    private UserStatus userStatus;


    public CustomUsersDetails(String userId, String userPassword, Collection<? extends GrantedAuthority> authorities) {
        super(userId, userPassword, authorities);
    }

    // enum 정의
    public enum UserSocial {
        NONE, GOOGLE, NAVER
    }

    public enum UserRole {
        USER, ADMIN
    }

    public enum UserStatus {
        NORMAL, BANNED
    }

    // OAuth2User와의 일관성을 위해 추가
    public String getName() {
        return this.nickname != null ? this.nickname : this.userName;
    }

}
