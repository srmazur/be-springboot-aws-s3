package springbootawss3.controller;

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

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestPart(value = "file")MultipartFile file){
        return this.amazonService.uploadFile(file);
    }

    @DeleteMapping("/deleteFile")
    public String deleteFile(@RequestPart(value = "url") String fileURL){
        return this.amazonService.deleteFileFromS3bucket(fileURL);
    }

    @GetMapping("/getFileList")
    public List<String> getFileList(){
        return amazonService.listFiles();
    }
}
