package com.dev.vocalab.wordbooks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordBookRepository extends JpaRepository<WordBookEntity, Integer> {

    // 사용자별 WordBook 조회
    List<WordBookEntity> findByUserId(String userId);

    // 북마크된 WordBook 조회
    List<WordBookEntity> findByBookmarkTrue();

    // 제목으로 WordBook 검색
    List<WordBookEntity> findByWordBookTitleContainingIgnoreCase(String wordBookTitle);
}
