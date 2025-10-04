package com.cheolhyeon.diary.auth.session;

import com.cheolhyeon.diary.auth.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<AuthSession, String> {
    Optional<AuthSession> findByUserId(Long userId);

    @Query(
            value = """
                    select *
                    from auth_session a
                        where a.session_id = :sid
                          and a.expires_at > :currentTime
                    limit 1
                    """,
            nativeQuery = true
    )
    Optional<AuthSession> findActiveSessionById(
            @Param("sid")String sid,
            @Param("currentTime")LocalDateTime currentTime
    );
}
