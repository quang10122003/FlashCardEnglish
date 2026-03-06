package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.BlogCategory;
import com.TestFlashCard.FlashCard.request.BlogCategoryCreateRequest;
import com.TestFlashCard.FlashCard.request.BlogCreateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.BlogResponse;
import com.TestFlashCard.FlashCard.service.BlogService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {
    @Autowired
    private final BlogService blogService;
    @Autowired
    private final ObjectMapper objectMapper;

    @GetMapping("/category/getAll")
    public ResponseEntity<?> getAllBlogCategory() throws IOException {
        List<BlogCategory> categories = blogService.getAllCategory();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(categories));
    }

    @PostMapping("/category/create")
    public ResponseEntity<?> createBlogCategory(@RequestBody BlogCategoryCreateRequest request) throws IOException {
        blogService.createCategory(request);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Created a new Blog Category: " + request.getTitle()));
    }

    @DeleteMapping("/category/delete/{id}")
    public ResponseEntity<?> deleteBlogCategory(@PathVariable Integer id) throws IOException {
        BlogResponse blog = blogService.getBlogById(id);
        blogService.deleteCategory(id);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Blog Category: " + blog.title()));
    }

    @PutMapping("/category/update/{id}")
    public ResponseEntity<?> updateBlogCategory(@PathVariable Integer id,
            @RequestBody BlogCategoryCreateRequest request) {
        blogService.updateCategory(request, id);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Blog Category has been updated!"));
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllBlog() throws IOException {
        List<BlogResponse> responses = blogService.getAllBlog();
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getBlogById(@PathVariable Integer id) throws IOException {
        BlogResponse response = blogService.getBlogById(id);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createBlog(@RequestPart String dataJson,
            @RequestPart(required = false) MultipartFile image) throws IOException {

        BlogCreateRequest request = objectMapper.readValue(dataJson, BlogCreateRequest.class);
        blogService.createBlog(request, image);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Created a new Blog: " + request.getTitle()));
    }

    @PutMapping(value = "update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateBlog(@RequestPart String dataJson,
            @RequestPart(required = false) MultipartFile image, @PathVariable Integer id) throws IOException {

        BlogCreateRequest request = objectMapper.readValue(dataJson, BlogCreateRequest.class);
        if (request.getCategory() == null || request.getDetail() == null || request.getShortDetail() == null
                || request.getTitle() == null)
            throw new BadRequestException("Cannot update the Blog with id: " + id + ". Fields must not be null!");
        blogService.updateBlog(request, image, id);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Blog has been updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Integer id) throws IOException {
        BlogResponse blog = blogService.getBlogById(id);
        blogService.deleteBlog(id);
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Blog: " + blog.title()));
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getByCategory(@RequestParam String category) throws IOException {
        return  ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(blogService.getByCategory(category)));
    }
}
