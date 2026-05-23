package com.publicpulse.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.publicpulse.app.model.User;

public interface UserRepository
        extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findByMobile(String mobile);

}