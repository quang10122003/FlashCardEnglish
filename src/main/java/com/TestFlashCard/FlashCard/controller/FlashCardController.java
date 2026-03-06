package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import com.TestFlashCard.FlashCard.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.request.FlashCardCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicCreateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardTopicUpdateRequest;
import com.TestFlashCard.FlashCard.request.FlashCardUpdateRequest;
import com.TestFlashCard.FlashCard.service.FlashCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flashcard")
@RequiredArgsConstructor
public class FlashCardController {
    @Autowired
    private final FlashCardService flashCardService;

    @GetMapping("/getTopicsByUser/{userID}")
    public ResponseEntity<?> getAllTopics(@PathVariable int userID) {
        List<ListFlashCardTopicResponse> topics = flashCardService.getFlashCardTopicsByUser(userID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(topics));
    }

    @GetMapping("/getFlashCardsByTopic/{topicID}")
    public ResponseEntity<?> getAllFlashCard(@PathVariable Integer topicID) {
        List<ListFlashCardsResponse> flashCards = flashCardService.getFlashCardsByTopic(topicID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCards));
    }

    @GetMapping("/topic/{id}")
    public ResponseEntity<?> getFlashCardTopicById(@PathVariable Integer id) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardTopicById(id)));
    }

    @GetMapping("/getTopicPopular")
    public ResponseEntity<?> getAllTopicPopular() throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardTopicByVisitCount()));
    }

    @PostMapping("/createFlashCard")
    public ResponseEntity<?> createFlashCard(@RequestBody @Valid FlashCardCreateRequest request) throws IOException{
        try {
            flashCardService.createFlashCard(request);
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("FlashCard created successfully"));
        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }

    @PostMapping("/createTopic")
    public ResponseEntity<?> createFlashCardTopic(@RequestBody @Valid FlashCardTopicCreateRequest request,
            Principal principal) throws IOException {
        flashCardService.createFlashCardTopic(request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Topic created successfully"));
    }
    @GetMapping("/checkTopicOfUser/{topicID}")
    public ApiResponse<?> checkFlashCardOfUser(@PathVariable Integer topicID, Principal principal) {

        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Check success",  flashCardService.checkTopicOfUser(topicID, principal.getName()));
        } catch (Exception exception) {
            return new ApiResponse<>().error(HttpStatus.BAD_REQUEST.value(),
                    "Check false because: " + exception.getMessage());
        }
    }
    @PutMapping("/updateTopic")
    public ResponseEntity<?> updateTopic(@RequestBody FlashCardTopicUpdateRequest request, Principal principal) {
        flashCardService.updateTopic(request, principal.getName());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Topcic updated successfully"));
    }

    @PutMapping("/updateFlashCard")
    public ResponseEntity<?> updateFlashCard(@RequestBody FlashCardUpdateRequest request) {
        flashCardService.updateFlashCard(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Topic updated successfully"));
    }

    @DeleteMapping("/deleteTopic/{id}")
    public ApiResponse<?> deleteTopicById(@PathVariable int id) {
        try {
            flashCardService.deleteTopic(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Topic deleted successfully");
        }catch(Exception e){
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Topic deleted failed because: "+e.getMessage());
        }
    }

    @DeleteMapping("/deleteFlashCard/{id}")
    public ResponseEntity<?> deleteFlashCardById(@PathVariable int id) {
        flashCardService.deleteFlashCard(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("FlashCard deleted successfully"));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getFlashCardById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(flashCardService.getFlashCardById(id)));
    }

    @PostMapping("/savePublishTopic/{topicID}")
    public ApiResponse<?> savePublishTopic(@PathVariable Integer topicID,Principal principal){
        try {
            flashCardService.savePublishTopic(topicID, principal.getName());
            return new ApiResponse<>(HttpStatus.ACCEPTED.value(),"Topic saved successfully",true );
        }catch(Exception e){
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Topic saved failed because: "+e.getMessage(),new ShareTopicResponse(false,"Topic saved failed because: "+e.getMessage()));
        }
    }
    @PatchMapping("/shareTopic/{topicID}")
    public ApiResponse<?> shareTopic(@PathVariable int topicID){
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Topic shared successfully",flashCardService.shareTopic(topicID));
        }catch(Exception e){
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Topic shared failed because: "+e.getMessage());
        }
    }
    
    @PutMapping("/raiseVisitCount/{id}")
    public ResponseEntity<?> raiseVisitCount(@PathVariable Integer id) {
        flashCardService.updateVisitCountTopic(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Success"));
    }
}
