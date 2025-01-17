package com.dev.vocalab.oauth2.users;

import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UsersService extends DefaultOAuth2UserService {
    //DefaultOAuth2UserService는 OAuth2UserService의 구현체이기 때문에 상속받음

    private final UsersRepository usersRepository;

    public CustomOAuth2UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    // 불러들인 gender값 변환 메서드
    private int convertGender(String provider, String userGender, String birthYear) {
        try {
            int year = Integer.parseInt(birthYear);
            boolean isBornAfter2000 = year >= 2000;

            if ("naver".equals(provider)) {
                if ("M".equals(userGender)) {
                    return isBornAfter2000 ? 3 : 1; // 2000년대 남자: 3, 1900년대 남자: 1
                } else if ("F".equals(userGender)) {
                    return isBornAfter2000 ? 4 : 2; // 2000년대 여자: 4, 1900년대 여자: 2
                }
            }
            else if ("google".equals(provider)) {
                if ("male".equalsIgnoreCase(userGender)) {
                    return isBornAfter2000 ? 3 : 1; // 2000년대 남자: 3, 1900년대 남자: 1
                } else if ("female".equalsIgnoreCase(userGender)) {
                    return isBornAfter2000 ? 4 : 2; // 2000년대 여자: 4, 1900년대 여자: 2
                }
            }

            // 성별이나 출생연도를 확인할 수 없는 경우
            throw new IllegalArgumentException("성별이나 출생연도 확인이 불가합니다.");

        } catch (NumberFormatException e) {
            // birthYear가 숫자로 변환할 수 없는 경우
            throw new IllegalArgumentException("유효하지 않은 데이터 타입(숫자로 변환 불가)");
        }
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // userRequest인자를 통해 구글 네이버 인증 데이터가 넘어옴

        OAuth2User oAuth2User = super.loadUser(userRequest); // 부모클래스에 존재하는 loadUser메서드에 userRequest를 넣고 유저정보를 가져옴
        System.out.println(oAuth2User.getAttributes()); // 가져온 OAuth2User 데이터 로깅

        // 넘어온 데이터가 어느 인증 서비스 provider인지 알기위해
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null; // 네이버 구글 인증 규격이 다르기 때문에 이를 따로 받을 DTO를 생성해야함
        if (registrationId.equals("naver")) {

            oAuth2Response = new NaverResponse(oAuth2User.getAttributes()); // 네이버 방식대로 oAuth2Response 바구니에 getAttributes로 데이터를 꺼내 넣음
        }
        else if (registrationId.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes()); // 구글 방식대로 oAuth2Response 바구니에 getAttributes로 데이터를 꺼내 넣음
        }
        else {

            return null;
        }

        String userName = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        UsersEntity existData = usersRepository.findByUserName(userName);

//        String userRole = "ROLE_USER";
        // UserRole enum 직접 사용
        UsersEntity.UserRole userRole = UsersEntity.UserRole.USER;

        // 처음 로그인
        if (existData == null) {
            UsersEntity usersEntity = new UsersEntity();
            usersEntity.setUserId(oAuth2Response.getUserEmail());
            usersEntity.setUserName(userName);
            usersEntity.setUserNickname(userName); // 임시로 이름을 닉네임으로, 후에 사용자가 수정하도록
            usersEntity.setUserEmail(oAuth2Response.getUserEmail());

            // 소셜 로그인 제공자 설정
            UsersEntity.UserSocial userSocial =
                    registrationId.equals("naver") ? UsersEntity.UserSocial.NAVER : UsersEntity.UserSocial.GOOGLE;
            usersEntity.setUserSocial(userSocial);

            usersEntity.setUserRole(userRole);

            // birthDate 처리
            String birthDate = oAuth2Response.getBirthYear() + "-" + oAuth2Response.getBirthday();
            usersEntity.setBirthDate(birthDate);

            // gender 처리
            int gender = convertGender(oAuth2Response.getProvider(), oAuth2Response.getGender(), oAuth2Response.getBirthYear());
            usersEntity.setGender(gender);

            usersRepository.save(usersEntity);
        } else { // 이미 존재하는

            // 업데이트
            existData.setUserName(userName);
            existData.setUserEmail(oAuth2Response.getUserEmail());

            // 기존 사용자의 role을 사용
            userRole = existData.getUserRole();

            usersRepository.save(existData);
        }

        return new CustomOAuth2Users(oAuth2Response, userRole); // 쿼리에는 이넘을 해놓긴했는데 좀따 염두해야할듯
    }

}
