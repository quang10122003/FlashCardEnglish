package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GetUserByFilterRequest {
    @Size(min = 1, message = "Invalid user's account")
    private String accountName;
    @Min(value = 1, message = "Invalid user's id")
    private Integer id;
}
