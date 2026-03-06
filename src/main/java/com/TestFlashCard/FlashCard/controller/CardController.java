package com.TestFlashCard.FlashCard.controller;

import java.io.IOException;
import java.security.Principal;

import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.response.CardFillResponse;
import com.TestFlashCard.FlashCard.response.FlashCardNomalResponse;
import com.TestFlashCard.FlashCard.service.DictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.TestFlashCard.FlashCard.request.CardCreateRequest;
import com.TestFlashCard.FlashCard.request.CardUpdateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.CardsResponse;
import com.TestFlashCard.FlashCard.service.CardService;
import com.TestFlashCard.FlashCard.service.MinIO_MediaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    @Autowired
    private final CardService cardService;
    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final MinIO_MediaService minIO_MediaService;
    @Autowired
    private final DictionaryService dictionaryService;

    @GetMapping("/detail/{cardID}")
    public ApiResponse<?> getCardDetail(@PathVariable Integer cardID) throws IOException {
        try {
            if (cardID == null)
                throw new IOException("Missing card's ID for this request");
            CardsResponse response = cardService.getCardDetail(cardID);
            // return
            // ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
            return new ApiResponse<>(HttpStatus.OK.value(), "View success", response);
        } catch (Exception exception) {
            return new ApiResponse<>().error(HttpStatus.BAD_REQUEST.value(),
                    "View detail card faild. message =" + exception.getMessage());
        }
    }

    @GetMapping("/getByFlashCard/{flashCardID}")
    public ApiResponse<?> getByFlashCard(@PathVariable Integer flashCardID) {

        try {
            FlashCardNomalResponse responses = cardService.getFlashCardDetail(flashCardID);
            return new ApiResponse<>(HttpStatus.OK.value(), "View success", responses);
        } catch (Exception exception) {
            return new ApiResponse<>().error(HttpStatus.BAD_REQUEST.value(),
                    "faild. message =" + exception.getMessage());
        }
    }


    @PostMapping(value = "/createCard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCard(@RequestPart(required = false) MultipartFile image,
            @RequestParam("data") String dataJson) throws IOException {

        try {
            // Transform string to json object
            CardCreateRequest request = objectMapper.readValue(dataJson, CardCreateRequest.class);

            cardService.createCard(request, image);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success("Created a new Card: " + request.getTerminology()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                            "Create a new Card failed. Exception = " + exception.getMessage()));
        }
    }

    @GetMapping("/fill")
    public ResponseEntity<?> fillCardData(@RequestParam String word) {
        try {
            CardFillResponse data = dictionaryService.fetchWordData(word);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Word not found: " + word));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                            "Error while fetching word data. Exception = " + ex.getMessage()));
        }
    }

    @PutMapping("/update/detail/{cardID}")
    public ApiResponse<?> updateCard(@PathVariable("cardID") Integer id, @RequestBody CardUpdateRequest request) {
        try {
            cardService.updateCardDetail(request, id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Card detail has been updated!");
        } catch (Exception exception) {
            return new ApiResponse<>().error(HttpStatus.BAD_REQUEST.value(),
                    "Card detail has been failed!. message =" + exception.getMessage());
        }
    }

    @PutMapping(value = "/update/image/{cardID}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeImage(@PathVariable Integer cardID, @RequestParam MultipartFile image)
            throws IOException {
        if (image != null) {
            String uniqueName = minIO_MediaService.uploadFile(image);
            cardService.changeImage(cardID, uniqueName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Card image has been updated!"));
    }

    @DeleteMapping("/delete/image/{cardID}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer cardID) {
        CardsResponse card = cardService.getCardDetail(cardID);
        cardService.deleteImage(cardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Card: " + card.definition()));
    }

    @DeleteMapping("/delete/card/{cardID}")
    public ResponseEntity<?> deleteCard(@PathVariable Integer cardID) {
        CardsResponse card = cardService.getCardDetail(cardID);
        cardService.deleteCard(cardID);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Deleted Card: " + card.terminology()));
    }

    // @PostMapping("/createListCard")
    // public ResponseEntity<?> createListCards(@RequestParam List<MultipartFile>
    // files) throws Exception {
    // List<String> imageUrls = new ArrayList<>();
    // return ResponseEntity.ok(null);
    // }

    @PutMapping("resetAll/{flashcardId}")
    public ResponseEntity<?> resetAllCards(@PathVariable Integer flashcardId) {
        cardService.resetListCard(flashcardId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("All Cards in this FlashCard has been reset."));
    }
}
