package com.example.db_project.storage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${app.s3.bucket}")
    private String bucket;

    /** 파일을 S3에 올리고, 저장된 키를 돌려준다 */
    public String upload(MultipartFile file) {
        String key = createKey(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new IllegalStateException("S3 업로드 실패: " + key, e);
        }
        return key;
    }

    /** key에 대한 5분짜리 조회 링크를 발급한다 */
    public String createViewUrl(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getRequest)
                .signatureDuration(Duration.ofMinutes(5))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /** covers/2026/11/550e8400-....png 형태의 키 생성 */
    private String createKey(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        LocalDate today = LocalDate.now();
        return "covers/%d/%02d/%s%s".formatted(
                today.getYear(), today.getMonthValue(), UUID.randomUUID(), ext);
    }
}
