package com.TestFlashCard.FlashCard.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ListIdContributeRequest {
    @NotEmpty
    private List<Integer> questionIds;
}
