package com.capstone1.sasscapstone1.service.FirebaseService;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public interface FirebaseService {
    String getImageUrl(String name);
    CompletableFuture<String> save(MultipartFile file, String originFileName) throws IOException;
    CompletableFuture<String> save(BufferedImage bufferedImage, String originalFileName) throws IOException;
    void delete(String name) throws IOException;
    default String getExtension(String originalFileName){
        return StringUtils.getFilenameExtension(originalFileName);
    }
    default String generateFileName(String originalFileName){
        return UUID.randomUUID().toString() + getExtension(originalFileName);
    }

    default byte[] getByteArrays(BufferedImage bufferedImage, String format) throws IOException{
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, format, byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }
}
