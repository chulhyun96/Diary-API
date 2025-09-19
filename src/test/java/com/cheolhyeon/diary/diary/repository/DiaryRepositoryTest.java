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

import java.time.LocalDateTime;
import java.util.List;


@DataJpaTest
@Transactional
@ActiveProfiles("test")
class DiaryRepositoryTest {
    @Autowired
    private DiaryRepository diaryRepository;

    private LocalDateTime testStart;
    private LocalDateTime testEnd;

    @BeforeEach
    void setUp() {
        diaryRepository.deleteAll(); // 일단 전부 조회해서 영속성 컨텍스트에 올린 뒤 삭제하기 때문에 findAll과 같은 쿼리가 발생한다.
        testStart = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        testEnd = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
    }
    @Test
    @DisplayName("특정 Month에 조회된 일기")
    void findByMonth() {
        //given
        byte[] id1 = UlidGenerator.generatorUlid();
        byte[] id2 = UlidGenerator.generatorUlid();
        byte[] id3 = UlidGenerator.generatorUlid();
        createTestDiary(id1,"일기1",testStart.plusDays(5));
        createTestDiary(id2,"일기2",testStart.plusDays(10));
        createTestDiary(id3,"일기3",testStart.plusDays(15));
        //when
        List<Diaries> result = diaryRepository.findByMonth(1L, testStart, testEnd);

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