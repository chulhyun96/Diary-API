package com.cheolhyeon.diary.diaries.service;

import com.cheolhyeon.diary.app.exception.user.UserException;
import com.cheolhyeon.diary.auth.repository.UserRepository;
import com.cheolhyeon.diary.diaries.dto.reqeust.DiaryRequest;
import com.cheolhyeon.diary.diaries.enums.Mood;
import com.cheolhyeon.diary.diaries.enums.Weather;
import com.cheolhyeon.diary.diaries.repository.DiaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;
@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {
    @Mock
    DiaryRepository diaryRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    DiaryService diaryService;

    @Test
    @DisplayName("UserId가 NULL일 경우 UerException NOT_FOUND 에러를 던진다")
    void throw_UserException_NotFound() {
        //given
        Long nonExistUserId = 1L;
        DiaryRequest request = buildDiaryRequest()
                .writerId(nonExistUserId)
                .build();
        given(userRepository.findById(nonExistUserId))
                .willReturn(Optional.empty());
        //when
        UserException userException = assertThrows(UserException.class,
                () -> diaryService.createDiary(request));

        //then
        assertThat(userException.getMessage())
                .contains("UserErrorStatus.NOT_FOUND")
                .contains("errorCode=404")
                .contains("errorMessage=Not Found")
                .contains("errorDescription=해당 유저는 회원이 아닙니다.");
        then(userRepository).should(times(1)).findById(nonExistUserId);
        then(diaryRepository).should(never()).save(any());
    }
    private static DiaryRequest.DiaryRequestBuilder buildDiaryRequest() {
        return DiaryRequest.builder()
                .writerId(1L)
                .writer("테스트작성자")
                .title("테스트 일기")
                .content("테스트 내용")
                .mood(Mood.HAPPY)
                .weather(Weather.SUNNY)
                .location("서울")
                .tags("테스트,일기");
    }
}