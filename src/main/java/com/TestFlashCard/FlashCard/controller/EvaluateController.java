package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.request.EvaluateCreateRequest;
import com.TestFlashCard.FlashCard.request.EvaluateUpdateByUserRequest;
import com.TestFlashCard.FlashCard.request.EvaluateUpdateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.EvaluateResponse;
import com.TestFlashCard.FlashCard.service.EvaluateService;
import com.TestFlashCard.FlashCard.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/evaluate")
public class EvaluateController {
    @Autowired
    private final EvaluateService evaluateService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createEvaluate(@RequestPart String data,
            @RequestPart(required = false) MultipartFile image, Principal principal) throws IOException {
        User user = userService.getUserByAccountName(principal.getName());
        EvaluateCreateRequest request = objectMapper.readValue(data, EvaluateCreateRequest.class);
        evaluateService.createEvaluate(request, image, user);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Created a new Evaluate!"));
    }

    @DeleteMapping("/delete/{evaluateID}")
    public ResponseEntity<?> deleteEvaluate(@PathVariable("evaluateID") Integer id) {
        evaluateService.deleteEvaluate(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Evaluate!"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getBy(@RequestParam(required = false) Integer star) throws IOException {
        List<EvaluateResponse> evaluates = star != null ? evaluateService.getEvaluatesByStar(star)
                : evaluateService.getAllEvaluates();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(evaluates));
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<?> updateEvaluate(@PathVariable Integer id, @RequestBody EvaluateUpdateRequest request)
            throws IOException {
        evaluateService.update(request.getAdminReply(), id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Evaluate has been updated!"));
    }

    @GetMapping("/loadByUser")
    public ResponseEntity<?> getByUser(Principal principal) throws IOException{
        User user = userService.getUserByAccountName(principal.getName());
        EvaluateResponse response = evaluateService.getByUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PutMapping(value = "/updateByUser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateByUser(@RequestPart String dataJson,
            @RequestPart(required = false) MultipartFile image, Principal principal) throws IOException {
        User user = userService.getUserByAccountName(principal.getName());
        
        EvaluateUpdateByUserRequest request = objectMapper.readValue(dataJson, EvaluateUpdateByUserRequest.class);
        evaluateService.updateByUser(user, request, image);
        return ResponseEntity.ok("Update " + user.getAccountName() + "'s evaluate successfully");
    }

}
