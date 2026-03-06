package com.TestFlashCard.FlashCard.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {

    public static String getPaymentUrlLikeVnPaySample(Map<String, String> vnpParams, String secretKey, String vnpUrl)
            throws UnsupportedEncodingException {

        // 1. Sắp xếp theo thứ tự ABC
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        // Dùng iterator để replicate logic "if (itr.hasNext()) { append '&' }" như sample
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Theo sample: họ ENCODE giá trị khi tạo hashData (US_ASCII)
                String encodedValueForHash = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());

                // Build hash data (dùng encodedValueForHash để match sample)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(encodedValueForHash);

                // Build query (theo sample họ cũng encode bằng US_ASCII)
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(encodedValueForHash);

                if (itr.hasNext()) {
                    // Sample chèn & dựa trên iterator.hasNext()
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // Sinh secure hash (sha512) trên hashData như sample
        String vnp_SecureHash = hmacSHA512(secretKey, hashData.toString());

        // Append secure hash vào query (không encode hash)
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        System.out.println("HASH DATA (sample-style) = " + hashData.toString());
        System.out.println("SECURE HASH = " + vnp_SecureHash);
        System.out.println("FINAL URL = " + vnpUrl + "?" + query.toString());
        return vnpUrl + "?" + query.toString();
    }

    public static String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                    new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    // Hàm ký lại tất cả field (dùng cho IPN)
    public static String hashAllFields(Map<String, String> fields, String secretKey) throws UnsupportedEncodingException {
        // 1️⃣ Sắp xếp theo thứ tự alphabet (A-Z)
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        // 2️⃣ Nối key=value và cách nhau bằng &
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName);
                hashData.append('=');
                // Encode theo US_ASCII để đồng nhất với VNPAY
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // 3️⃣ Hash SHA512
        return hmacSHA512(secretKey, hashData.toString());
    }


    public static String removeVietnameseAccent(String s) {
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\s]", ""); // bỏ ký tự đặc biệt
    }
}