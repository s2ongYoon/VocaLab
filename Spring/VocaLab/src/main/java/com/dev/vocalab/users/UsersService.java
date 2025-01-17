package com.dev.vocalab.users;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void loginUser(String userId, String userPassword) {
        UsersEntity users = new UsersEntity();
        users.setUserId(userId);
        users.setUserPassword(passwordEncoder.encode(userPassword));

        System.out.println("로그인 되었습니다(userId, userPassword): " + userId + ", " + userPassword);
    }

    // 회원가입 처리
    public void registerUser(String userId
            , String userName, String userPassword, String userNickname,
                                    String userEmail, String year, String month, String day, Integer gender){

        UsersEntity users = new UsersEntity();

        users.setUserId(userId);
        users.setUserName(userName);
        users.setUserPassword(passwordEncoder.encode(userPassword));
        users.setUserNickname(userNickname);
        users.setUserEmail(userEmail);

        // 생년월일 형식 변환 (YYYY-MM-DD)
        String birthDate = String.format("%s-%s-%s",
                year,
                month.length() == 1 ? "0" + month : month,
                day.length() == 1 ? "0" + day : day
        );
        users.setBirthDate(birthDate);

        // 성별 코드 변환
        int birthYear = Integer.parseInt(year);
        int formattedGender;
        if (birthYear < 2000) {
            // 2000년 이전 출생
            formattedGender = (gender == 1) ? 1 : 2;
        } else {
            // 2000년 이후 출생
            formattedGender = (gender == 1) ? 3 : 4;
        }
        users.setGender(formattedGender);

        usersRepository.save(users);

        System.out.println
                ("회원가입 되었습니다(userId, userName, userPassword, userNickname, userEmail, birthDate, gender): "
         + userId + " " + userName + " " + userPassword + " " +
                userNickname  + " " + userEmail + " " + birthDate + " " + gender);
    }
}
