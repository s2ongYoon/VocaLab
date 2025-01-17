package com.dev.vocalab.oauth2.users;

import java.util.Map;

// DTO
public class GoogleResponse implements OAuth2Response{

    // json 데이터를 받을 Map형식의 변수 선언
    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return "goggle";
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString(); // 구글은 id가아닌 sub이 키워드
    }

    @Override
    public String getUserEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getUserName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getGender() {
        return attribute.get("gender").toString();
    }

    @Override
    public String getBirthday() {
        return attribute.get("birthday").toString();
    }

    @Override
    public String getBirthYear() {
        return attribute.get("birthYear").toString();
    }
}