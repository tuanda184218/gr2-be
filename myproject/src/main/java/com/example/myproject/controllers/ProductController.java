package com.example.myproject.controllers;

import com.example.myproject.models.Product;
import com.example.myproject.models.ResponseObject;
import com.example.myproject.models.User;
import com.example.myproject.repository.ProductRepository;
import com.example.myproject.repository.UserRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(path = "/api/products")
public class ProductController {
    @Autowired
    private ProductRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @GetMapping("")
    List<Product> getAllProducts() {
        return repository.findAll();
    }

    //insert new Product with POST method
    //Postman : Raw, JSON
//    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ResponseObject> createProduct(@RequestParam("productName") String productName,
                                                 @RequestParam("description") String description,
                                                 @RequestParam("price") double price,
                                                 @RequestParam("image") MultipartFile image,
                                                 @RequestParam("userId") Long userId) throws IOException {
        Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), Objects.requireNonNull(image.getOriginalFilename()));
        image.transferTo(tempFile);

        if(FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("/firebase.json"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, image.getOriginalFilename()).build();
            storage.create(blobInfo, Files.readAllBytes(tempFile));

            Files.delete(tempFile);
        }

        Product product = new Product();
        product.setProductName(productName);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(String.format("https://storage.googleapis.com/%s/%s", bucketName, image.getOriginalFilename()));
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        product.setUser(user);
        Product savedProduct = repository.save(product);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Insert Product successfully", savedProduct)
        );
    }


    //Get detail product with id
    @GetMapping("/{id}")
    //Let's return an object with: data, message, status
    ResponseEntity<ResponseObject> findById(@PathVariable Long id) {
        Optional<Product> foundProduct = repository.findById(id);
        return foundProduct.isPresent() ?
                ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("ok", "Query product successfully", foundProduct)
                        //you can replace "ok" with your defined "error code"
                ):
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject("failed", "Cannot find product with id = "+id, "")
                );
    }

    //get all product in userId
    @GetMapping("/user/{userId}")
    ResponseEntity<ResponseObject> getProductsByUserId(@PathVariable Long userId) {
        List<Product> products = repository.findByUserId(userId);

        return products.isEmpty() ?
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ResponseObject("failed", "Cannot find product with userId = "+userId, "")
                ):
                ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject("ok", "Query product successfully", products)
                        //you can replace "ok" with your defined "error code"
                );
    }


    //update, upsert = update if found, otherwise insert
//    @PutMapping("/{id}")
//    ResponseEntity<ResponseObject> updateProduct(@RequestBody Product newProduct, @PathVariable Long id) {
//        Product updatedProduct = repository.findById(id)
//                .map(product -> {
//                    product.setProductName(newProduct.getProductName());
//                    product.setDescription(newProduct.getDescription());
//                    product.setPrice(newProduct.getPrice());
//                    product.setImage(newProduct.getImage());
//                    return repository.save(product);
//                }).orElseGet(() -> {
//                    newProduct.setId(id);
//                    return repository.save(newProduct);
//                });
//        return ResponseEntity.status(HttpStatus.OK).body(
//                new ResponseObject("ok", "Update Product successfully", updatedProduct)
//        );
//    }

    @PutMapping("/{id}")
    ResponseEntity<ResponseObject> updateProduct(@RequestParam("productName") String productName,
                                                 @RequestParam("description") String description,
                                                 @RequestParam("price") double price,
                                                 @RequestParam("image") MultipartFile image,
                                                 @PathVariable Long id
                                                 ) throws IOException {
        Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), Objects.requireNonNull(image.getOriginalFilename()));
        image.transferTo(tempFile);

        if(FirebaseApp.getApps().isEmpty()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("/firebase.json"));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, image.getOriginalFilename()).build();
            storage.create(blobInfo, Files.readAllBytes(tempFile));

            Files.delete(tempFile);
        }

                Product updatedProduct = repository.findById(id)
                .map(product -> {
                    product.setProductName(productName);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setImage(String.format("https://storage.googleapis.com/%s/%s", bucketName, image.getOriginalFilename()));
                    return repository.save(product);
                }).orElseGet(() -> {
                    Product newProduct = new Product();
                    newProduct.setId(id);
                    return repository.save(newProduct);
                });
        return ResponseEntity.status(HttpStatus.OK).body(
                new ResponseObject("ok", "Update Product successfully", updatedProduct)
        );

    }

    //Delete a Product => DELETE method
    @DeleteMapping("/{id}")
    ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long id) {
        boolean exists = repository.existsById(id);
        if(exists) {
            repository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject("ok", "Delete product successfully", "")
            );
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("failed", "Cannot find product to delete", "")
        );
    }
}