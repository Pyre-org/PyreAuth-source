package com.pyre.auth.service;

import com.pyre.auth.dto.request.ImageUploadResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;

public interface S3Service {
    @Transactional
    String s3Upload(ImageUploadResponse dto) throws IOException;
}