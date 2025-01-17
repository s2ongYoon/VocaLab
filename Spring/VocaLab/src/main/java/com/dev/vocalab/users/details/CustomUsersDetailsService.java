package com.dev.vocalab.users.details;

import com.dev.vocalab.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomUsersDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // DB에서 userId를 가진 유저를 찾음
        var result = usersRepository.findByUserId(userId);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException("존재하지 않는 사용자입니다.");
        }

        var usersEntity = result.get();

        // 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usersEntity.getUserRole().name()));

        // CustomUser 객체 생성 및 반환
        CustomUsersDetails customUsersDetails = new CustomUsersDetails(
                usersEntity.getUserId(),
                usersEntity.getUserPassword(),
                authorities
        );

        // 추가 정보 설정
        customUsersDetails.setUserName(usersEntity.getUserName());
        customUsersDetails.setNickname(usersEntity.getUserNickname());
        customUsersDetails.setEmail(usersEntity.getUserEmail());
        customUsersDetails.setBirthDate(usersEntity.getBirthDate());
        customUsersDetails.setGender(usersEntity.getGender());

        customUsersDetails.setUserSocial(CustomUsersDetails.UserSocial.valueOf(usersEntity.getUserSocial().name()));
        customUsersDetails.setUserRole(CustomUsersDetails.UserRole.valueOf(usersEntity.getUserRole().name()));
        customUsersDetails.setUserStatus(CustomUsersDetails.UserStatus.valueOf(usersEntity.getUserStatus().name()));

        return customUsersDetails;
    }
}
