package com.dt.khohang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

import com.dt.khohang.service.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(
            CustomOAuth2UserService customOAuth2UserService,
            ClientRegistrationRepository clientRegistrationRepository) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth

                // Trang login và tài nguyên giao diện
                .requestMatchers(
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**"
                ).permitAll()

                // ADMIN
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // CUSTOMER & ADMIN
                .requestMatchers("/customer/**")
                .hasAnyRole("CUSTOMER", "ADMIN")

                // Những trang còn lại phải đăng nhập
                .anyRequest().authenticated()
            )

            // Login username/password
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )

            // Google Login (OIDC vì scope có openid)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .authorizationEndpoint(auth -> auth
                    .authorizationRequestResolver(
                        googleAuthorizationRequestResolver()
                    )
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOAuth2UserService)
                )
            )

            // Logout - xóa sạch session và cookie
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }

    /**
     * Buộc Google hiện màn hình chọn tài khoản
     * thay vì tự động đăng nhập luôn
     */
    private OAuth2AuthorizationRequestResolver googleAuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository,
                    "/oauth2/authorization"
                );

        resolver.setAuthorizationRequestCustomizer(customizer ->
            customizer.additionalParameters(params ->
                params.put("prompt", "select_account")
            )
        );

        return resolver;
    }
}