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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AmazonService {
    private AmazonS3 s3client;

    @Value("${amazon.s3.region}")
    private String region;
    @Value("${amazon.s3.bucket}")
    private String bucketName;
    @Value("${amazon.aws.access-key-id}")
    private String accessKey;
    @Value("${amazon.aws.access-key-secret}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon(){
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public String uploadFile(MultipartFile multipartFile){
        String fileURL = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile);
            uploadFileToS3bucket(fileName, file);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileURL;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException{
        File convertFile = new File(file.getOriginalFilename());
        FileOutputStream fileOutputStream = new FileOutputStream(convertFile);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.close();
        return convertFile;
    }

    private String generateFileName(MultipartFile file){
        return Date.from(Instant.now()).getTime() + "-"
                + file.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileToS3bucket(String fileName, File file){
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    private String deleteFileFromS3bucket(String fileURL){
        String fileName = fileURL.substring(fileURL.lastIndexOf("/")+1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        return "Successfully deleted: " + fileName;
    }

    public List<String> listFiles(){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName).withPrefix("/");

        List<String> keys = new ArrayList<>();
        ObjectListing listing = s3client.listObjects(listObjectsRequest);
        while (true){
            List<S3ObjectSummary> summaries = listing.getObjectSummaries();
            if(summaries.isEmpty()){
                break;
            }
            for(S3ObjectSummary item: summaries){
                if(!item.getKey().endsWith("/")){
                    keys.add(item.getKey());
                }
            }
            listing = s3client.listNextBatchOfObjects(listing);
        }
        return keys;
    }


}
