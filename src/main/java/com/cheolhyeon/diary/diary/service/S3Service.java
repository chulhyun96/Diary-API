package com.cheolhyeon.diary.diary.service;

import com.cheolhyeon.diary.app.exception.s3.S3ErrorStatus;
import com.cheolhyeon.diary.app.exception.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public List<String> upload(Long writerId, List<MultipartFile> images) {
        List<String> keys = new ArrayList<>();
        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String key = generateKey(writerId, image.getOriginalFilename(), i + 1);
                keys.add(key);
                s3Template.upload(bucketName, key, image.getInputStream());
            }
        } catch (Exception e) {
            String errorMessageS3Key = "";
            for (String k : keys) {
                errorMessageS3Key = k;
                delete(k);
            }
            throw new S3Exception(S3ErrorStatus.FAILED_UPLOAD_IMAGE, errorMessageS3Key);
        }
        return keys;
    }

    private String generateKey(Long writerId, String originalName, int order) {
        final String prefix = "diary_service";
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
        return "%s/%d/%s/%d/%s.%s".formatted(prefix, writerId, date, order, fileName, ext);
    }

    public void delete(String k) {
        s3Template.deleteObject(bucketName, k);
    }
}
