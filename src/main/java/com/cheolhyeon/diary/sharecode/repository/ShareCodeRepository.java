package com.cheolhyeon.diary.sharecode.repository;

import com.cheolhyeon.diary.sharecode.entity.ShareCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShareCodeRepository extends JpaRepository<ShareCode, Long> {
    @Query(
            value = """
                    select * from share_code s
                    where s.user_id = :userId
                    and s.status = 'ACTIVE'
                    """, nativeQuery = true
    )
    Optional<ShareCode> findShareCodeById(@Param("userId")Long userId);
}
