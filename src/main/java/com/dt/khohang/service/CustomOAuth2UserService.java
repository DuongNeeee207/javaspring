package com.dt.khohang.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.dt.khohang.entity.User;
import com.dt.khohang.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();

        // Tìm hoặc tạo user mới trong DB
        User user = userRepository.findByUsername(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(email);
                    newUser.setPassword(""); // OAuth2 không cần password
                    newUser.setRole("CUSTOMER");
                    return userRepository.save(newUser);
                });

        // Trả về OidcUser với role từ DB
        return new DefaultOidcUser(
                Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole())
                ),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}
