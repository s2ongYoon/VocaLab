package com.vocalab.www.Board;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name = "board")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boardId", nullable = false)
    private Integer id;

    @Column(name = "userId", nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parentId")
    private Integer parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "replyStatus", nullable = false)
    private ReplyStatus replyStatus;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files;

    public enum Category {
        NOTICE, FAQ, INQUIRY, REPLY
    }

    public enum ReplyStatus {
        NONE, DONE
    }
}
