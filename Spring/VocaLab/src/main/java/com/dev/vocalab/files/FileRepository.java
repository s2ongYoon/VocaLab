package com.dev.vocalab.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// FileRepository.java
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    @Query("SELECT COUNT(f) > 0 FROM FileEntity f WHERE f.filePath = :filePath")
    boolean existsByFilePath(@Param("filePath") String filePath);

    List<FileEntity> findByTableIdAndCategory(Integer tableId, FileEntity.Category category);

    @Transactional
    void deleteByTableIdAndCategory(Integer tableId, FileEntity.Category category);
}
