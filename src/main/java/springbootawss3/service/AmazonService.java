package springbootawss3.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.model.AmazonS3Exception;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AmazonService {
    private AmazonS3 s3client;

    @Value("${AWS_REGION}")
    private String region;
    @Value("${AWS_ACCESS_KEY_ID}")
    private String accessKey;
    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String createBucket(String bucketNameToCreate) {
//        initializeAmazon();
        try {
            if (!s3client.doesBucketExist(bucketNameToCreate)) {
                s3client.createBucket(bucketNameToCreate);
                return "Bucket created successfully: " + bucketNameToCreate;
            } else {
                return "Bucket already exists: " + bucketNameToCreate;
            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
            return "Error in creating bucket: " + e.getMessage() + s3client.getS3AccountOwner().getDisplayName();
        }
    }

    public String deleteBucket(String bucketNameToDelete) {
        try {
            ObjectListing objectListing = s3client.listObjects(bucketNameToDelete);
            while (true) {
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    s3client.deleteObject(bucketNameToDelete, objectSummary.getKey());
                }
                if (objectListing.isTruncated()) {
                    objectListing = s3client.listNextBatchOfObjects(objectListing);
                } else {
                    break;
                }
            }
            s3client.deleteBucket(bucketNameToDelete);
            return "Bucket deleted successfully: " + bucketNameToDelete;
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
            return "Error in deleting bucket: " + e.getMessage();
        }
    }

    public String uploadFile(MultipartFile multipartFile, String bucketName) {
        String fileURL = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            uploadFileToS3bucket(fileName, file, bucketName);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileURL;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(convertFile);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return convertFile;
    }

    private String generateFileName(MultipartFile file) {
        return Date.from(Instant.now()).getTime() + "-"
                + file.getOriginalFilename().replace(" ", "_");
    }

    public CompletableFuture<String> uploadFileToS3bucket(String fileName, File file, String bucketName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
                return "File uploaded successfully: " + fileName;
            } catch (AmazonS3Exception e) {
                throw new RuntimeException("Error uploading file: " + e.getMessage());
            }
        });
    }

    public String deleteFileFromS3bucket(String fileName, String bucketName) {
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted: " + fileName;
    }


    public List<String> listFiles(String bucketName) {
        List<String> fileNames = new ArrayList<>();
        ObjectListing objectListing = s3client.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName));

        while (true) {
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                fileNames.add(objectSummary.getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = s3client.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
        return fileNames;
    }

    public List<Bucket> listBuckets() {
        return s3client.listBuckets();
    }

}
