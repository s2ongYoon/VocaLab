package com.vocalab.www.Board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
	// 기본적인 CRUD 기능 지원

	// 전체 레코드 인출 및 정렬
	List<BoardEntity> findAll(Sort sort);

	// 페이징 적용(검색 X)
	Page<BoardEntity> findAll(Pageable pageable);

	// 페이징 적용(검색 O)
	Page<BoardEntity> findByTitleLike(String keyword, Pageable pageable);

	// 특정 게시판에 연관된 파일 검색
	List<FileEntity> findFilesByBoardId(Integer boardId);

	// 특정 게시판 및 카테고리에 따른 파일 검색
	List<FileEntity> findFilesByBoardIdAndCategory(Integer boardId, FileEntity.Category category);
}
