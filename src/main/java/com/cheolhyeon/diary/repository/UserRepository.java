package com.cheolhyeon.diary.repository;

import com.cheolhyeon.diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, byte[]> {


    Optional<User> findByOauth2Id(String oauth2Id);

    boolean existsByOauth2Id(String oauth2Id);
} 