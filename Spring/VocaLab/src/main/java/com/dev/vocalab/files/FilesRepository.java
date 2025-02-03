package com.dev.vocalab.files;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FilesRepository extends JpaRepository<FilesEntity, Long> {

    // [ 단어 추가시 파일 존재 여부 확인 ]
    FilesEntity findByUserIdAndTableIdAndCategory(String userId, int tableId, String category);

    //void deleteByUploadedAt(LocalDateTime minus);
    // [ home.jsp 단어 추출 기록 ]
    List<FilesEntity> findByUserId(String userId);

    // [ compile_result.jsp 단어장 삭제 ]
    // 삭제할 단어가 존재하는지 확인
    List<FilesEntity> findByTableIdAndCategoryAndUserId(int wordbookId, String category, String userId);
    void deleteByTableIdAndCategoryAndUserId(int wordbookId, String category, String userId);


    // 추출 기록 중 7일이 지난 원본파일 찾기(경로,파일Id)
//    List<CompileFilesEntity> fineBYUploadedAtAndCategory(LocalDateTime sevenDaysAgo, String compile);

    // 찾은 원본 파일 삭제
//    void deleteByFileId(int fileId);
}
