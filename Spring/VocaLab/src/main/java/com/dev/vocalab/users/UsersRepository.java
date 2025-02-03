package com.dev.vocalab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, String> {

    // 사용자 ID와 상태로 검색
    UsersEntity findByUserId(String userId);
}