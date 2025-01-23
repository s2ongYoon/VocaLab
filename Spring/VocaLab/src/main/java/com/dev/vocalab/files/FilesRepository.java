package com.dev.vocalab.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FilesRepository extends JpaRepository<FilesEntity, Long> {

    @Query("SELECT COUNT(f) > 0 FROM FilesEntity f WHERE f.filePath = :filePath")
    boolean existsByFilePath(@Param("filePath") String filePath);

//    Board에서 boardId로 tableId 값 조회
    List<FilesEntity> findByTableIdAndCategory(Integer tableId, FilesEntity.Category category);

//    Board 에서 내용 지울때 image file도 같이 file 테이블에서 지우기 위해 부르는 메서드
    @Transactional
    void deleteByTableIdAndCategory(Integer tableId, FilesEntity.Category category);
}
