package com.cheolhyeon.diary.diary.repository;


import com.cheolhyeon.diary.diary.entity.Diaries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diaries, byte[]> {
    @Query(value = """
            select d.* from diaries d
            where d.writer_id = :writerId
            and d.created_at >= :startDay
            and d.created_at < :endDay
            ORDER BY d.created_at DESC
            """,
            nativeQuery = true
    )
    List<Diaries> findByMonthAndDay(
            @Param("writerId") Long writerId,
            @Param("startDay") LocalDateTime startDay,
            @Param("endDay") LocalDateTime endDay
    ); // 2025/09/19 00:00 ~ 19 23:59

    Optional<Diaries> findById(byte[] diaryId);

    @Query(value = """
                    select * from diaries d
                    where d.writer_id = :writerId
                    and d.created_at >= :startMonth
                    and d.created_at < :endMonth
                    ORDER BY d.created_at DESC;
            """, nativeQuery = true
    )
    List<Diaries> findAllByYearAndMonth(
            @Param("writerId") Long writerId,
            @Param("startMonth") LocalDateTime startMonth,
            @Param("endMonth") LocalDateTime endMonth);
}
