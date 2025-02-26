package com.capstone1.sasscapstone1.service.FirebaseService;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseServiceImpl implements FirebaseService{
    @Value("${firebase.image-url}")
    private String imageUrl;
    @Value("${firebase.bucket-name}")
    private String bucketName;
    @Override
    public String getImageUrl(String name) {
        return String.format(imageUrl, name);
    }

    @Override
    public CompletableFuture<String> save(MultipartFile file, String originalFileName) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket(bucketName);

        String fileName = generateFileName(originalFileName);

        String contentType = file.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String downloadUrl;
        try {
           Blob blob= bucket.create(fileName, file.getBytes(), contentType);
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            downloadUrl= String.format(imageUrl, fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Firebase: " + e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(downloadUrl);
    }

    @Override
    public CompletableFuture<String> save(BufferedImage bufferedImage, String originalFileName) throws IOException {
        byte[] bytes= getByteArrays(bufferedImage,getExtension(originalFileName));

        Bucket bucket= StorageClient.getInstance().bucket(bucketName);

        String fileName = generateFileName(originalFileName);

        String downloadUrl;
        try {
            Blob blob=bucket.create(fileName,bytes);
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            downloadUrl= String.format(imageUrl, fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Firebase: " + e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(downloadUrl);
    }

    @Override
    @Async
    public void delete(String name) throws IOException {
        Bucket bucket= StorageClient.getInstance().bucket(bucketName);
        if(!StringUtils.hasText(name)){
            throw new IOException("invalid file name");
        }
        Blob blob= bucket.get(name);
        if (blob==null){
            throw new IOException("file not found");
        }
        blob.delete();
    }

    public String generateFileName(String originalFileName) {
        // Tạo một tên file duy nhất
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(originalFileName);
        return uuid + "_" + originalFileName.replace(" ", "_") + (extension.isEmpty() ? "" : "." + extension);
    }

    public String getExtension(String fileName) {
        return StringUtils.getFilenameExtension(fileName);
    }
}
