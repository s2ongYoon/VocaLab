package com.dev.vocalab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, String> {

    // 사용자 이메일로 검색
    UsersEntity findByUserEmail(String userEmail);

    // 사용자 닉네임으로 검색
    UsersEntity findByUserNickname(String userNickname);

    // 사용자 ID와 상태로 검색
    UsersEntity findByUserIdAndUserStatus(String userId, UsersEntity.UserStatus userStatus);

    // 사용자 ID가 특정 값으로 시작하는 모든 사용자 검색
    List<UsersEntity> findByUserIdStartingWith(String prefix);

    // 사용자 소셜 타입으로 검색
    List<UsersEntity> findByUserSocial(UsersEntity.UserSocial userSocial);

    // 활성화된 사용자만 검색 (NORMAL 상태)
    List<UsersEntity> findByUserStatus(UsersEntity.UserStatus userStatus);

}
