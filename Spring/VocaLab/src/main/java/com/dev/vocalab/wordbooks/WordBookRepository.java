package com.dev.vocalab.wordbooks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordBookRepository extends JpaRepository<WordBooksEntity, Integer> {
    // [ 사용자 단어장 목록 조회 ]
    List<WordBooksEntity> findByUserId(String userId);
    // [ 사용자 단어장 삭제 ]
    void deleteByWordBookId(int wordbookId);

    WordBooksEntity findByWordBookId(int wordBookId);
}