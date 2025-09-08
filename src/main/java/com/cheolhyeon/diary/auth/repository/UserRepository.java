package com.cheolhyeon.diary.auth.repository;

import com.cheolhyeon.diary.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
