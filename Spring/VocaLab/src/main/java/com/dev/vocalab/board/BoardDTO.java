package com.dev.vocalab.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardDTO {
    private Integer boardId;
    private BoardEntity.Category category;         // ENUM 값을 String으로 저장
    private String title;            // 게시글 제목
    private String content;          // 게시글 내용
    private Integer parentId;        // 부모 게시글 ID
    private BoardEntity.ReplyStatus replyStatus;      // ENUM 값을 String으로 저장
    private LocalDateTime createdAt; // 생성일자
    private LocalDateTime updatedAt; // 수정일자
    private String userNickname;// 사용자 닉네임

}
