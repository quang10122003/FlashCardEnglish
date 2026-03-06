package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.Utils.VNPayUtil;
import com.TestFlashCard.FlashCard.entity.PaymentTransaction;
import com.TestFlashCard.FlashCard.entity.User;
import com.TestFlashCard.FlashCard.repository.PaymentTransactionRepository;
import com.TestFlashCard.FlashCard.request.PaymentCreateRequest;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.response.PaymentTransactionResponse;
import com.TestFlashCard.FlashCard.service.PaymentService;
import com.TestFlashCard.FlashCard.service.UserService;
import com.TestFlashCard.FlashCard.service.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayServiceImpl vnPayService;
    private final UserService userService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentService paymentService;
    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;
    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @PostMapping("/vnpay/create")
    public ApiResponse<?> createVnpayPayment(
            @RequestBody PaymentCreateRequest requestBody,
            HttpServletRequest request) {
        try {
            Long amount = requestBody.getAmount();
            String description = requestBody.getDescription();
            String startDate = requestBody.getStartDate();
            String endDate = requestBody.getEndDate();
            String clientIp = vnPayService.getClientIp(request);
            // Tạo tạm transaction để lưu start/end trước khi thanh toán
            PaymentTransaction tempTransaction = paymentService.createTempTransaction(amount, description,
                    startDate,endDate);

            // Tạo URL thanh toán VNPAY
            String paymentUrl = vnPayService.createPayment(amount,tempTransaction.getOrderId(),description, clientIp, tempTransaction.getId());

            return new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Trả về url thành công", paymentUrl);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                    "Trả về url thất bại vì " + e.getMessage());
        }
    }
    // API 1: Chi tiết giao dịch theo txnRef
    @GetMapping("/{txnRef}")
    public ApiResponse<?> getPaymentByTxnRef(@PathVariable String txnRef) {
        try {
            PaymentTransaction tx = paymentService.findByOrderId(txnRef);
            return new ApiResponse<>(HttpStatus.OK.value(), "Thành công", paymentService.toDTO(tx));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                    "Thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/checkExpiration")
    public ApiResponse<?> checkExpiration(@RequestParam Long userId) {
        try {
            List<PaymentTransaction> payments = paymentService.findAllByUserIdAndTransactionStatus(userId);
            boolean hasValidPayment = payments.stream()
                    .anyMatch(tx -> tx.getEndDate() != null && tx.getEndDate().isAfter(LocalDateTime.now()));
            return new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                    Map.of("hasValidPayment", hasValidPayment));

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                    "Thất bại: " + e.getMessage());
        }
    }
    @GetMapping("/validPayments")
    public ApiResponse<?> getValidPayments(@RequestParam Long userId) {
        try {
            List<PaymentTransaction> payments = paymentService.findAllByUserIdAndTransactionStatus(userId);

            // Lọc các payment còn hạn và chuyển sang DTO
            List<PaymentTransactionResponse> validPayments = payments.stream()
                    .filter(tx -> tx.getEndDate() != null && tx.getEndDate().isAfter(LocalDateTime.now()))
                    .map(paymentService::toDTO)  // dùng hàm toDTO của bạn
                    .toList();

            return new ApiResponse<>(HttpStatus.OK.value(), "Thành công", validPayments);

        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Thất bại: " + e.getMessage());
        }
    }
    @GetMapping("/vnpay-ipn")
    public ApiResponse<Map<String, String>> handleVnpayIPN(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // B1: Lấy toàn bộ tham số gửi về từ VNPAY
            Map<String, String> fields = new HashMap<>();
            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    fields.put(fieldName, fieldValue);
                }
            }

            // B2: Lấy chữ ký từ VNPAY
            String vnp_SecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // B3: Ký lại dữ liệu để so sánh
            String signValue = VNPayUtil.hashAllFields(fields, vnp_HashSecret);

            if (signValue.equals(vnp_SecureHash)) {
                // ✅ Check chữ ký hợp lệ
                String vnp_TxnRef = fields.get("vnp_TxnRef");
                String vnp_ResponseCode = fields.get("vnp_ResponseCode");
                String vnp_Amount = fields.get("vnp_Amount");

                // TODO: kiểm tra dữ liệu thực trong DB
                boolean checkOrderId = true;
                boolean checkAmount = true;
                boolean checkOrderStatus = true;

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(vnp_ResponseCode)) {
                                response.put("RspCode", "00");
                                response.put("Message", "Confirm Success");
                                //upadte db
                            } else {
                                response.put("RspCode", "00");
                                response.put("Message", "Payment Failed");
                            }
                        } else {
                            response.put("RspCode", "02");
                            response.put("Message", "Order already confirmed");
                        }
                    } else {
                        response.put("RspCode", "04");
                        response.put("Message", "Invalid Amount");
                    }
                } else {
                    response.put("RspCode", "01");
                    response.put("Message", "Order not Found");
                }
            } else {
                // ❌ Sai chữ ký
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
            }

            return new ApiResponse<>(HttpStatus.OK.value(), "Trả về data thành công", response);

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Trả về data thất bại", response);
        }
    }

    @GetMapping("/vnpay-return")
    public void handleVnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> fields = new HashMap<>();
        Map<String, String[]> paramMap = request.getParameterMap();

        for (String key : paramMap.keySet()) {
            String[] values = paramMap.get(key);
            if (values != null && values.length > 0 && !values[0].isEmpty()) {
                fields.put(key, values[0]);
            }
        }

        log.info("===== [VNPAY RETURN] =====");
        log.info("Params: {}", fields);

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String feBase = "https://english-vocabulary-system.vercel.app/VnpayResult";
        String redirectUrl;

        try {
            String signValue = VNPayUtil.hashAllFields(fields, vnp_HashSecret);
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String vnp_TxnRef = fields.get("vnp_TxnRef");
            String vnp_Amount = fields.get("vnp_Amount");
            String vnp_BankCode = fields.get("vnp_BankCode");
            String vnp_OrderInfo = fields.get("vnp_OrderInfo");
            String vnp_TransactionNo = fields.get("vnp_TransactionNo");

            if (signValue.equals(vnp_SecureHash)) {
                if ("00".equals(vnp_ResponseCode)) {
                    log.info("✔ Thanh toán thành công cho đơn {}", vnp_TxnRef);

                    // Lấy temp transaction lưu start/end trước đó
                    PaymentTransaction tempTransaction = paymentService.findByOrderId(vnp_TxnRef);
                    tempTransaction.setAmount(Long.parseLong(vnp_Amount) / 100);
                    tempTransaction.setPaymentMethod("VNPAY");
                    tempTransaction.setCurrency("VND");
                    tempTransaction.setBankCode(vnp_BankCode);
                    tempTransaction.setTransactionCode(vnp_TransactionNo);
                    tempTransaction.setResponseCode(vnp_ResponseCode);
                    tempTransaction.setTransactionStatus("SUCCESS");
                    tempTransaction.setDescription(vnp_OrderInfo);
                    tempTransaction.setTransactionDate(LocalDateTime.now());
                    tempTransaction.setSecureHash(vnp_SecureHash);

                    paymentTransactionRepository.save(tempTransaction);
                    log.info("✔ Lưu giao dịch vào DB thành công: {}", tempTransaction.getOrderId());

                    redirectUrl = String.format("%s/%s", feBase, vnp_TxnRef);

                } else {
                    redirectUrl = String.format("%s?status=fail&code=%s&orderId=%s",
                            feBase, vnp_ResponseCode, vnp_TxnRef);
                }
            } else {
                redirectUrl = String.format("%s?status=invalid-signature&orderId=%s", feBase, vnp_TxnRef);
            }

        } catch (Exception e) {
            log.error("Lỗi xử lý VNPay return: {}", e.getMessage(), e);
            redirectUrl = feBase + "?status=server-error";
        }

        response.setHeader("ngrok-skip-browser-warning", "anyvalue");
        response.sendRedirect(redirectUrl);
    }
}


