package com.cheolhyeon.diary.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class S3RollbackCleanup {
    private List<String> imageKeys;
}
