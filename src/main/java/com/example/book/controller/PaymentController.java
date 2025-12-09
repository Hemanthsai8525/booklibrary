package com.example.book.controller;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final String KEY_ID = "YOUR_KEY_ID";
    private final String KEY_SECRET = "YOUR_SECRET";

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody(required = false) JSONObject data) {
        try {
            RazorpayClient client = new RazorpayClient(KEY_ID, KEY_SECRET);

            JSONObject orderReq = new JSONObject();
            orderReq.put("amount", data.getInt("amount") * 100); 
            orderReq.put("currency", "INR");
            orderReq.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = client.orders.create(orderReq);

            return ResponseEntity.ok(order.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating order");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody JSONObject payload) {
        try {
            String orderId = payload.getString("razorpay_order_id");
            String paymentId = payload.getString("razorpay_payment_id");
            String signature = payload.getString("razorpay_signature");

            String secret = KEY_SECRET;
            String payloadString = orderId + "|" + paymentId;

            String expectedSignature = Utils.getHash(payloadString, secret);

            if (expectedSignature.equals(signature)) {
                return ResponseEntity.ok("Payment Verified");
            }
            return ResponseEntity.status(400).body("Invalid Signature");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying payment");
        }
    }
}
