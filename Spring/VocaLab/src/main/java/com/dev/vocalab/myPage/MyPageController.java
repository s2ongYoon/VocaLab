package com.dev.vocalab.myPage;

import com.dev.vocalab.users.details.AuthenticationUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/mypage")
@Controller
public class MyPageController {

    @RequestMapping("/compileHistory")
    public String myPageRecord() {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }


        return "myPageHistory";
    }

    @RequestMapping("/userInformation")
    public String myPageUserInfo() {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }


        return "myPageUserInfo";
    }

    @RequestMapping("/userModify")
    public String myPageUserModify() {

        // 인증 확인
        if (!AuthenticationUtil.isAuthenticated()) {
            return "redirect:/login";
        }


        return "myPageUserModify";
    }

} // class
