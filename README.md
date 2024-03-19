This sample application showcases a REST API for AWS, specifically designed for interaction with Amazon S3. Developed using Spring Boot, the application provides functionality to upload files to, delete files from, and list files in an AWS S3 Bucket through RESTful services.

To utilize this application, certain prerequisites must be met:

1. AWS Account Configuration: An active AWS account is required to access S3 services. If you do not have an account, please create one on the Amazon website to get started.
2. Create an IAM User and Assign Permissions: For secure access to the S3 Bucket, create an IAM user and assign the necessary permissions for S3 Bucket access. This step ensures the application interacts with S3 securely under a controlled access policy.
3. Have on your machine aws cli , mvn 
4. Run the application : ./start-application.sh

Create Bucket:          `curl -X POST "http://localhost:8080/storage/createBucket?bucketName={bucketName}"`
Upload File:            `curl -X POST -F "file=@{/path/to/your/file.txt}" -F "bucketName={bucketName}" http://localhost:8080/storage/uploadFile`
Delete File:            `curl -X DELETE "http://localhost:8080/storage/deleteFile?bucketName={bucketName}t&fileName={fileName}"`
Delete Bucket:          `curl -X DELETE "http://localhost:8080/storage/deleteBucket?bucketName={bucketName}"`
List Buckets:           `curl -X GET "http://localhost:8080/storage/listBuckets"`
List Items in a Bucket: `curl -X GET "http://localhost:8080/storage/listItems?bucketName={bucketName}"`





