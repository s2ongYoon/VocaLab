package com.dev.vocalab.home;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
//        user
        session.setAttribute("sessionId", "user001");
        session.setAttribute("sessionRole", "USER");
        session.setAttribute("sessionNickName", "USER001");

//        admin
//        session.setAttribute("sessionId", "testAdmin");

        String userId = (String) session.getAttribute("sessionId");
        // [ 회원 정보 불러오기 ]
//        UsersEntity user = homeService.selectUser(userId);
//        model.addAttribute("user", user);

        // [ 회원일 경우 compileRecord 표시 ]
        if (userId != null) {
//            List<CompileDTO> compileDtoList = homeService.selectRecord(userId);
//            System.out.println("compileRecordList : "+compileDtoList);
//            model.addAttribute("compileDtoList", compileDtoList);
        }
        return "home";
    }

}
