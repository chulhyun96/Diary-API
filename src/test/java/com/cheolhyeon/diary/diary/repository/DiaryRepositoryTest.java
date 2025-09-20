package com.cheolhyeon.diary.diary.repository;

import com.cheolhyeon.diary.app.util.UlidGenerator;
import com.cheolhyeon.diary.diary.entity.Diaries;
import com.cheolhyeon.diary.diary.enums.Mood;
import com.cheolhyeon.diary.diary.enums.Weather;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@DataJpaTest
@Transactional
@ActiveProfiles("test")
class DiaryRepositoryTest {
    @Autowired
    private DiaryRepository diaryRepository;

    private LocalDateTime testStart;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll();
        testStart = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
    }
    @Test
    @DisplayName("특정 Month, Day에 조회된 일기")
    void findByMonth() {
        //given
        byte[] id1 = UlidGenerator.generatorUlid();
        byte[] id2 = UlidGenerator.generatorUlid();
        byte[] id3 = UlidGenerator.generatorUlid();
        LocalDate currentDate = LocalDate.of(2024, 1, 1);
        LocalDateTime startDay = currentDate.atStartOfDay();
        LocalDateTime endDay = startDay.plusDays(1);

        createTestDiary(id1,"일기1",testStart.plusHours(1));
        createTestDiary(id2,"일기2",testStart.plusHours(2));
        createTestDiary(id3,"일기3",testStart.plusHours(3));

        //when
        List<Diaries> result = diaryRepository.findByMonthAndDay(
                1L,startDay, endDay);

        //then
        Assertions.assertEquals(3, result.size());
        //최신순 정렬
        Assertions.assertEquals("일기3", result.get(0).getTitle());
        Assertions.assertEquals("일기2", result.get(1).getTitle());
        Assertions.assertEquals("일기1", result.get(2).getTitle());
    }

    private void createTestDiary(byte[] diaryId, String title, LocalDateTime updatedAt) {
        Diaries diary = Diaries.builder()
                .writerId(1L)
                .diaryId(diaryId)
                .writer("테스트유저")
                .title(title)
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .createdAt(updatedAt.minusDays(1))
                .updatedAt(updatedAt)
                .build();
        diaryRepository.save(diary);
    }
}