package br.com.ifrn.ddldevs.pets_backend.service;

import com.amazonaws.services.s3.AmazonS3;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadImageService {

    @Value("bucket-name")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadImg(MultipartFile multipartFile) {
        String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();

        try {
            File file = this.convertMultipartToFile(multipartFile);
            s3Client.putObject(bucketName, fileName, file);
            file.delete();
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "null";
        }
    }

    private File convertMultipartToFile(MultipartFile multipartFile) throws IOException {
        File convertFile = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertFile);
        fos.write(multipartFile.getBytes());
        fos.close();
        return convertFile;
    }
}