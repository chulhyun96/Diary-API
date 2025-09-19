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
            and d.updated_at >= :start
            and d.updated_at < :end
            ORDER BY d.updated_at DESC
            """,
            nativeQuery = true
    )
    List<Diaries> findByMonth(
            @Param("kakaoId") Long kakaoId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
