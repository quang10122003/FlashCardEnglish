package com.TestFlashCard.FlashCard.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCreateRequest {
    private Long amount;
    private String description;
    private String startDate;
    private String endDate;
}
