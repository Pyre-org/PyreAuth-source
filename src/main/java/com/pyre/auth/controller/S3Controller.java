package com.pyre.auth.controller;

import com.pyre.auth.dto.request.ImageUploadResponse;
import com.pyre.auth.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name="S3", description = "S3 API 구성")
@RequestMapping("/auth-service/s3")
public class S3Controller {
    private final S3Service s3Service;
    @PostMapping("/upload")
    @Operation(description = "이미지 업로드 엔드포인트")
    public String uploadImage(@ModelAttribute ImageUploadResponse dto) {
        try {
            return this.s3Service.s3Upload(dto);
        } catch (Exception ex) {
            return null;
        }
    }
}
