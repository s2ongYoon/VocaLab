package com.dev.vocalab.myPage;

import com.dev.vocalab.users.details.AuthenticationUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/mypage")
@Controller
public class MyPageController {

    @RequestMapping("/")
    public String myPage() {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }


        return "myPage";
    }

} // class
