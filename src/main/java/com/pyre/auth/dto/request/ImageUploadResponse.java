package com.pyre.auth.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadResponse(
        MultipartFile multipartFile
) {
}
