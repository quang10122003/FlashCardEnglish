package com.TestFlashCard.FlashCard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Entity
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;             // Mã đơn hàng hệ thống
    private Long amount;                // Số tiền
    private String currency;            // Loại tiền tệ (VND, USD,...)
    private String paymentMethod;       // Cổng thanh toán (VNPAY, MOMO, PAYPAL,...)
    private String bankCode;            // Mã ngân hàng (nếu có)
    private String transactionCode;     // Mã giao dịch từ cổng thanh toán
    private String responseCode;        // Mã phản hồi (VD: 00 = thành công)
    private String transactionStatus;   // Trạng thái giao dịch (PENDING, SUCCESS, FAILED)
    private String description;         // Nội dung thanh toán
    private LocalDateTime transactionDate; // Ngày giờ giao dịch
    private String secureHash;          // Mã hash (nếu có)
    // 🔗 Thêm quan hệ với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime startDate;  // thêm
    private LocalDateTime endDate;
}