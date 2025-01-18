package com.dev.vocalab.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// FileRepository.java
public interface FilesRepository extends JpaRepository<FilesEntity, Long> {
    @Query("SELECT COUNT(f) > 0 FROM FilesEntity f WHERE f.filePath = :filePath")
    boolean existsByFilePath(@Param("filePath") String filePath);

    List<FilesEntity> findByTableIdAndCategory(Integer tableId, FilesEntity.Category category);

    @Transactional
    void deleteByTableIdAndCategory(Integer tableId, FilesEntity.Category category);
}
