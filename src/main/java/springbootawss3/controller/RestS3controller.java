package springbootawss3.controller;

import com.amazonaws.services.s3.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springbootawss3.service.AmazonService;

import java.util.List;

@RestController
@RequestMapping("/storage/")
public class RestS3controller {
    @Autowired
    private AmazonService amazonService;

    @PostMapping("/createBucket")
    public String createBucket(@RequestParam(value = "bucketName") String bucketName) {
        return amazonService.createBucket(bucketName);
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file") MultipartFile file,
                             @RequestPart(value = "bucketName") String bucketName) {
        return this.amazonService.uploadFile(file, bucketName);
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestParam String fileName, @RequestParam String bucketName) {
        return this.amazonService.deleteFileFromS3bucket(fileName, bucketName);
    }

    @DeleteMapping("/deleteBucket")
    public String deleteBucket(@RequestParam(value = "bucketName") String bucketName) {
        return amazonService.deleteBucket(bucketName);
    }

    @GetMapping("/listItems")
    public List<String> listItems(@RequestParam String bucketName) {
        return amazonService.listFiles(bucketName);
    }

    @GetMapping("/listBuckets")
    public List<Bucket> bucketList() {
        return amazonService.listBuckets();
    }
}
