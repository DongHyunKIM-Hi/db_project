package com.example.db_project.storage;

import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final S3StorageService s3StorageService;

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String key = s3StorageService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("key", key));
    }

    /** 조회 링크 발급 — 프론트엔드가 img src에 넣을 때 */
    @GetMapping("/url")
    public ResponseEntity<Map<String, String>> viewUrl(@RequestParam("key") String key) {
        return ResponseEntity.ok(Map.of("url", s3StorageService.createViewUrl(key)));
    }

    /** 즉시 열기 — 브라우저 주소창에서 바로 확인할 때 */
    @GetMapping("/view")
    public ResponseEntity<Void> view(@RequestParam("key") String key) {
        URI signedUrl = URI.create(s3StorageService.createViewUrl(key));
        return ResponseEntity.status(302).location(signedUrl).build();
    }
}
