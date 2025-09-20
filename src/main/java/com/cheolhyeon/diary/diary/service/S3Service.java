package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.s3.S3ErrorStatus;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
import com.cheolhyeon.diary.diary.entity.Diaries;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Template s3Template;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public List<String> upload(Long writerId, byte[] diaryId, List<MultipartFile> images) {
        List<String> keys = new ArrayList<>();
        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);

                String key = generateKey(writerId, image.getOriginalFilename(), diaryId, i + 1);
                keys.add(key);
                s3Template.upload(bucketName, key, image.getInputStream());
            }
        } catch (Exception e) {
            List<String> failedKeys = new ArrayList<>();
            for (String k : keys) {
                failedKeys.add(k);
                delete(k);
            }
            throw new S3Exception(S3ErrorStatus.FAILED_UPLOAD_IMAGE, failedKeys);
        }
        return keys;
    }

    private String generateKey(Long writerId, String originalName, byte[] diaryId, int order) {
        final String s3ObjectName = "diary_service";
        String diaryIdAsString = UUID.nameUUIDFromBytes(diaryId).toString();
        String date = LocalDate.now().toString().replace("-", "/");
        String ext = Optional.ofNullable(originalName)
                .filter(it -> it.contains("."))
                .map(it -> it.substring(it.lastIndexOf(".") + 1).toLowerCase())
                .filter(s -> !s.isBlank())
                .orElse("bin");
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        String fileName = originalName != null && originalName.startsWith("thumbnail_")
                ? "thumbnail_" + key
                : key;
        return "%s/%d/%s/%s/%d/%s.%s".formatted(s3ObjectName, writerId, diaryIdAsString, date, order, fileName, ext);
    }

    public void delete(String k) {
        s3Template.deleteObject(bucketName, k);
    }

    public List<String> getThumbnailImageKey(Long writerId, int year, int month, int day, List<Diaries> diariesByMonth) {
        List<String> thumbnailImage = new ArrayList<>();

        for (Diaries diaries : diariesByMonth) {
            String diaryId = UUID.nameUUIDFromBytes(diaries.getDiaryId()).toString();
            String prefix = String.format("diary_service/%d/%s/%d/%02d/%02d/", writerId, diaryId, year, month, day);

            List<S3Resource> s3Resources = s3Template.listObjects(bucketName, prefix);
            for (S3Resource s3Resource : s3Resources) {
                String fullName = s3Resource.getFilename();
                String filename = StringUtils.getFilename(fullName);
                if (filename.startsWith("thumbnail_")) {
                    thumbnailImage.add(fullName);
                }
            }
        }
        return thumbnailImage;
    }

    public List<String> createImageUrl(List<String> imageJsonArray) {
        List<String> imageUrl = new ArrayList<>();
        try {
            for (String imageJson : imageJsonArray) {
                URL signedGetURL = s3Template.createSignedGetURL(bucketName, imageJson, Duration.ofMinutes(10L));
                String url = signedGetURL.toString();
                imageUrl.add(url);
            }
        } catch (Exception e) {
            throw new S3Exception(S3ErrorStatus.FAILED_LOAD_IMAGE, imageJsonArray);
        }
        return imageUrl;
    }
}
