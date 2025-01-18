package com.dev.vocalab.board;

import com.dev.vocalab.files.FilesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
    // 기본적인 CRUD 기능 지원

    // 전체 레코드 인출 및 정렬
    List<BoardEntity> findAll(Sort sort);

    // 페이징 적용(검색 X)
    Page<BoardEntity> findAll(Pageable pageable);

    Page<BoardEntity> findByCategory(BoardEntity.Category category, Pageable pageable);

    // 페이징 적용(검색 O)
    Page<BoardEntity> findByTitleContaining(String keyword, Pageable pageable);

    // 특정 게시판에 연관된 파일 검색 (JPQL 사용)
    @Query("SELECT f FROM FilesEntity f WHERE f.tableId = :boardId")
    List<FilesEntity> findFilesByBoardId(@Param("boardId") Integer boardId);

    // 특정 게시판 및 카테고리에 따른 파일 검색 (JPQL 사용)
    @Query("SELECT f FROM FilesEntity f WHERE f.tableId = :boardId AND f.category = :category")
    List<FilesEntity> findFilesByBoardIdAndCategory(
            @Param("boardId") Integer boardId,
            @Param("category") FilesEntity.Category category
    );
    @Query("SELECT b FROM BoardEntity b JOIN FETCH b.user WHERE b.parentId = :parentId")
    Optional<BoardEntity> findByParentId(@Param("parentId") Integer parentId);
    Page<BoardEntity> findByTitleLike(String title, Pageable pageable);

    @Query("SELECT new com.dev.vocalab.board.BoardDTO(" +
            "b.boardId, b.category, b.title, b.content, b.parentId, " +
            "b.replyStatus, b.createdAt, b.updatedAt, b.user.userNickname) " +
            "FROM BoardEntity b WHERE b.boardId = :boardId")
    Optional<BoardDTO> findBoardWithUserNicknameByBoardId(@Param("boardId") Integer boardId);

}