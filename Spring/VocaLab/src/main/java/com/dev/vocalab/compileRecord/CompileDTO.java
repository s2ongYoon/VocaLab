package com.dev.vocalab.compileRecord;

import com.dev.vocalab.files.FilesEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompileDTO {

    private int compileId; //일렬번호
    private String userId;
    private String source; //text box에 직접 입력한 url이나 text 데이터
    private LocalDateTime createdAt;

    private List<FilesEntity> fileRecordList;

    // json 결과
    private String word;
    private String pos;
    private String[] meanings;


}
