package com.vocalab.www.Board;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name = "file")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId", nullable = false)
    private Integer id;

    @Column(name = "userId", nullable = false, length = 100)
    private String userId;

    @Column(name = "tableId")
    private Integer tableId;

    @Column(name = "table", length = 100)
    private String table;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "fileType", nullable = false)
    private FileType fileType;

    @Column(name = "filePath", nullable = false, length = 255)
    private String filePath;

    @Column(name = "uploadedAt", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "boardId", nullable = false)
    private BoardEntity board;

    public enum Category {
        DOCUMENT, IMAGE, VIDEO, OTHER
    }

    public enum FileType {
        PDF, JPG, PNG, MP4, DOCX, OTHER
    }
}
