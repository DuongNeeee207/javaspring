package com.dt.khohang.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dt.khohang.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

}