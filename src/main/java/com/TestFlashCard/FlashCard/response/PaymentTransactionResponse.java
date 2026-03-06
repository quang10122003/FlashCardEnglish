package com.TestFlashCard.FlashCard.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PaymentTransactionResponse {
    private String orderId;
    private Long amount;
    private String currency;
    private String paymentMethod;
    private String bankCode;
    private String transactionCode;
    private String responseCode;
    private String transactionStatus;
    private String description;
    private LocalDateTime transactionDate;
    private String userName;

    // Thêm ngày bắt đầu và kết thúc
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
