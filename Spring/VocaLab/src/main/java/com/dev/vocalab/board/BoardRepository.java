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
//     전체 레코드 인출 및 정렬
    List<BoardEntity> findAll(Sort sort);

//     페이징 적용(검색 X)
    Page<BoardEntity> findAll(Pageable pageable);
    Page<BoardEntity> findByCategory(BoardEntity.Category category, Pageable pageable);
//    답변글이 있는지 조회
    @Query("SELECT b FROM BoardEntity b JOIN FETCH b.user WHERE b.parentId = :parentId")
    Optional<BoardEntity> findByParentId(@Param("parentId") Integer parentId);

//    검색이 있을시 카테고리를 읽고, 그 카테고리에 해당하며 제목도 검색에 맞춰서 조회
    @Query("SELECT b FROM BoardEntity b WHERE b.title LIKE :title AND b.category = :category")
    Page<BoardEntity> findByTitleLikeAndCategory(@Param("title") String title,
                                                 @Param("category") BoardEntity.Category category,
                                                 Pageable pageable);

//    게시글 단일로 불러올 때, boardId로 조회하며 닉네임까지 같이 조회
@Query("SELECT new com.dev.vocalab.board.BoardDTO(" +
        "b.boardId, b.user.userId, b.category, b.title, b.content, b.parentId, " +
        "b.replyStatus, b.createdAt, b.updatedAt, b.user.userNickname) " +
        "FROM BoardEntity b WHERE b.boardId = :boardId")
Optional<BoardDTO> findBoardWithUserNicknameByBoardId(@Param("boardId") Integer boardId);


    @Query("SELECT b FROM BoardEntity b WHERE b.category = :category AND b.user.userId = :userId")
    Page<BoardEntity> findAllByCategoryAndUserId(@Param("category") BoardEntity.Category category,
                                                 @Param("userId") String userId,
                                                 Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.category = :category AND b.user.userId = :userId AND (b.title LIKE :searchWord OR b.content LIKE :searchWord)")
    Page<BoardEntity> findAllByCategoryAndUserIdAndSearch(@Param("searchWord") String searchWord,
                                                          Pageable pageable,
                                                          @Param("category") BoardEntity.Category category,
                                                          @Param("userId") String userId);

    @Query("SELECT b FROM BoardEntity b WHERE b.user.userId = :userId AND b.category = :category")
    Page<BoardEntity> findByUserIdAndCategory(Pageable pageable, @Param("userId") String userId, @Param("category") BoardEntity.Category category);


    /*//     특정 게시판에 연관된 파일 검색 (JPQL 사용)
    @Query("SELECT f FROM FilesEntity f WHERE f.tableId = :boardId")
    List<FilesEntity> findFilesByBoardId(@Param("boardId") Integer boardId);
    //     특정 게시판 및 카테고리에 따른 파일 검색 (JPQL 사용)
    @Query("SELECT f FROM FilesEntity f WHERE f.tableId = :boardId AND f.category = :category")
    List<FilesEntity> findFilesByBoardIdAndCategory(
            @Param("boardId") Integer boardId,
            @Param("category") FilesEntity.Category category
    );*/

}