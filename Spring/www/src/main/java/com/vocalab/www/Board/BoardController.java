package com.vocalab.www.Board;

import org.springframework.web.bind.annotation.GetMapping;

public class BoardController {
    @GetMapping("/CS/Home")
    public String boardHome(){
        return "";
    }
}
