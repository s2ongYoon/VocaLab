package com.dev.vocalab.config;

import com.dev.vocalab.oauth2.users.CustomOAuth2Users;
import com.dev.vocalab.oauth2.users.CustomOAuth2UsersService;
import com.dev.vocalab.users.UsersEntity;
import com.dev.vocalab.users.UsersRepository;
import com.dev.vocalab.users.details.CustomUsersDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UsersService customOAuth2UsersService;
    private final UsersRepository usersRepository;

    public SecurityConfig(CustomOAuth2UsersService customOAuth2UsersService, UsersRepository usersRepository) {
        this.customOAuth2UsersService = customOAuth2UsersService;
        this.usersRepository = usersRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());

        http.formLogin(form -> form
                .loginPage("/login")                // 로그인 페이지 URL
                .loginProcessingUrl("/login-proc")  // 로그인 처리 URL
                .usernameParameter("userId")        // 아이디 파라미터명
                .passwordParameter("userPassword")   // 비밀번호 파라미터명
                .defaultSuccessUrl("/main")         // 로그인 성공 시 이동
                .failureUrl("/login?error")         // 로그인 실패 시 이동
                .permitAll());

        http.httpBasic((basic) -> basic.disable());

        http.oauth2Login(oauth2 ->
                oauth2.loginPage("/login")
//                        .clientRegistrationRepository(customClientRegistrationRepo.clientRegistrationRepository())
//                        .authorizedClientService(customOAuth2AuthorizedClientService.oAuth2AuthorizedClientService(jdbcTemplate, customClientRegistrationRepo.clientRegistrationRepository()))
                       .userInfoEndpoint((userInfoEndpointConfig) ->
                               userInfoEndpointConfig.userService(customOAuth2UsersService)));

        http.authorizeHttpRequests((authorize) ->
                authorize.requestMatchers("/**").permitAll()
        );

        return http.build();
    }

    // 해싱 DI
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
