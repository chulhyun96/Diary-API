package com.cheolhyeon.diary.diaries.repository;


import com.cheolhyeon.diary.diaries.entity.Diaries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diaries, Long> {
}
