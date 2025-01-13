package com.dev.vocalab.board;

import com.dev.vocalab.files.FileEntity;
import com.dev.vocalab.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Board")
@Data
@ToString(exclude = {"user", "files"})
@EqualsAndHashCode(exclude = {"user", "files"})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boardId", nullable = false)
    private Integer boardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "ENUM('NOTICE', 'FAQ', 'INQUIRY', 'REPLY') DEFAULT 'NOTICE'")
    private Category category;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parentId")
    private Integer parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "replyStatus", nullable = false, columnDefinition = "ENUM('NONE', 'DONE') DEFAULT 'NONE'")
    private ReplyStatus replyStatus;

    @Column(name = "createdAt", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private UserEntity user;

    // Getter & Setter
    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }



    public enum Category {
        NOTICE, FAQ, INQUIRY, REPLY
    }

    public enum ReplyStatus {
        NONE, DONE
    }
}
