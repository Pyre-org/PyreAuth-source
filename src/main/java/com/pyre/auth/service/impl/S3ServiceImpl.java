package com.pyre.auth.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.pyre.auth.config.S3Config;
import com.pyre.auth.dto.request.ImageUploadResponse;
import com.pyre.auth.exception.customexception.CustomException;
import com.pyre.auth.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final AmazonS3Client amazonS3Client;
    @Value("${spring.aws.s3.bucket}")
    private String bucket;
    @Value("${image.upload}")
    private String dirName;
    @Override
    // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
    public String s3Upload(ImageUploadResponse dto) throws IOException {
        File uploadFile = convert(dto.multipartFile())
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }
    private String upload(File uploadFile, String dirName) {
        String fileName = UUID.randomUUID() + uploadFile.getName().substring(uploadFile.getName().lastIndexOf("."));
        String uploadImageUrl = null;
        try {
            uploadImageUrl = putS3(uploadFile, fileName);
        } catch (Exception ex) {
            removeNewFile(uploadFile);
            return "이미지가 업로드 되지 않았습니다.";
        }

        removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)	// PublicRead 권한으로 업로드 됨
        );
        String link = amazonS3Client.getUrl(bucket, fileName).toString();
        String cloudfront = "https://dqgtky3fkqa5j.cloudfront.net";
        return cloudfront + link.substring(link.indexOf("/",15));
    }

    private void removeNewFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        }else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws  IOException {
        File convertFile = new File(dirName + "/" + file.getOriginalFilename());
        boolean isImage = false;

        try {

            String contentType = Files.probeContentType(convertFile.toPath()); // text/html, text/plain, image/jpeg

            isImage = contentType.startsWith("image");

        } catch(Exception e) {
            e.printStackTrace();
        }
        if (!isImage) throw new CustomException("이미지를 제외한 파일은 업로드 할 수 없습니다.");

        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
