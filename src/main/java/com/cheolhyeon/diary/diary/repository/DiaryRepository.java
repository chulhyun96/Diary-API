package com.cheolhyeon.diary.diary.repository;


import com.cheolhyeon.diary.diary.entity.Diaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diaries, byte[]> {
    @Query(value = """
            select d.* from diaries d
            where d.writer_id = :kakaoId
            and d.updated_at >= :startDay
            and d.updated_at < :endDay
            ORDER BY d.updated_at DESC
            """,
            nativeQuery = true
    )
    List<Diaries> findByMonthAndDay(
            @Param("kakaoId") Long kakaoId,
            @Param("startDay") LocalDateTime startDay,
            @Param("endDay") LocalDateTime endDay
            ); // 2025/09/19 00:00 ~ 19 23:59
}
