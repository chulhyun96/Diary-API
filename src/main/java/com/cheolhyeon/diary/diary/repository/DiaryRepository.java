package com.cheolhyeon.diary.diary.repository;


import com.cheolhyeon.diary.diary.entity.Diaries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diaries, Long> {
}
