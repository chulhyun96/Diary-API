package com.cheolhyeon.diary.token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<UsersJwt, Long> {
}
