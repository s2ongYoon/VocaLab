package com.dev.vocalab.files;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name="Files")
@Entity
@Data
@ToString
public class FilesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fileId;

    private String userId;
    private int tableId;
    private String category;

    private String fileType;
    private String filePath;
    private LocalDateTime uploadedAt;

    public enum Category {
        BOARD, COMPILE, ESSAY, NONE, PROFILE, TEST, TESTRECORD, WORD, COMPILERESULT
    }

    public enum FileType {
        FILE, IMAGE
    }

    // insert 시 업로드 날짜 자동 입력
    @PrePersist
    protected void onPrePersist() {
        //작성일 : 현재시각으로 설정
        this.uploadedAt = LocalDateTime.now();
    }
}
