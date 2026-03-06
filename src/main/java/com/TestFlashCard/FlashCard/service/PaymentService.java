package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.entity.PaymentTransaction;
import com.TestFlashCard.FlashCard.entity.ToeicQuestion;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.exception.ResourceNotFoundException;
import com.TestFlashCard.FlashCard.repository.PaymentTransactionRepository;
import com.TestFlashCard.FlashCard.response.PaymentTransactionResponse;
import com.TestFlashCard.FlashCard.response.ToeicQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserService userService;

    public PaymentTransaction findByOrderId(String txnRef) {
        return paymentTransactionRepository.findByOrderId(txnRef).orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction not found"));
    }
    public List<PaymentTransaction> findAllByUserIdAndTransactionStatus(Long userId) {
        return paymentTransactionRepository.findAllByUserIdAndTransactionStatus(userId, "SUCCESS");
    }
    public PaymentTransactionResponse toDTO(PaymentTransaction tx) {
        return new PaymentTransactionResponse(
                tx.getOrderId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getPaymentMethod(),
                tx.getBankCode(),
                tx.getTransactionCode(),
                tx.getResponseCode(),
                tx.getTransactionStatus(),
                tx.getDescription(),
                tx.getTransactionDate(),
                tx.getUser() != null ? tx.getUser().getFullName() : null,
                tx.getStartDate(),  // thêm ngày bắt đầu
                tx.getEndDate()     // thêm ngày kết thúc
        );
    }

    public PaymentTransaction createTempTransaction(Long amount, String description,
                                                    String startDate, String endDate) throws Exception {
        // Chuyển startDate và endDate từ String sang LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate, formatter).atStartOfDay();

        // Lấy current user từ Security Context
        User currentUser = userService.getCurrentUser();

        // Tạo orderId tạm: 12 ký tự đầu của UUID + "_" + userId
        String orderId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12)
                + "_" + currentUser.getId();

        // Tạo transaction tạm
        PaymentTransaction tempTx = new PaymentTransaction();
        tempTx.setOrderId(orderId);
        tempTx.setAmount(amount);
        tempTx.setDescription(description);
        tempTx.setTransactionStatus("PENDING"); // chưa thanh toán
        tempTx.setStartDate(start);
        tempTx.setEndDate(end);
        tempTx.setUser(currentUser);
        tempTx.setTransactionDate(LocalDateTime.now());

        // Lưu vào DB và trả về
        return paymentTransactionRepository.save(tempTx);
    }

    /**
     * Lấy transaction tạm theo ID
     */
//    public PaymentTransaction findTempTransactionById(Long tempTransactionId) throws Exception {
//        Optional<PaymentTransaction> optionalTx =
//                paymentTransactionRepository.findById(tempTransactionId);
//        if (optionalTx.isPresent()) {
//            return optionalTx.get();
//        } else {
//            throw new Exception("Temp transaction not found: " + tempTransactionId);
//        }
//    }
}
