package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FileEntity;
import com.dev.vocalab.files.FileRepository;
import com.dev.vocalab.files.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/WordBook")
@Controller
@RequiredArgsConstructor
public class WordBookController {
    private final WordBookService wordBookService;
    private final WordBookRepository wordBookRepository;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @GetMapping("/List")
    public String wordbookList(String userId){

        return "wordbooks/wordbooksList";
    }
}
