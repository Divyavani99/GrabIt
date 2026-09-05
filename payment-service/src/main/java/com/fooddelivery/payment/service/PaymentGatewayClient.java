package com.fooddelivery.payment.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Stand-in for a real PSP integration (Stripe/Razorpay/etc). Simulates network latency
 * and a small failure rate so the SUCCESS/FAILED branches in the rest of the system are exercised.
 */
@Component
public class PaymentGatewayClient {

    public GatewayResult charge(BigDecimal amount, String method) {
        boolean success = ThreadLocalRandom.current().nextDouble() > 0.05; // ~95% success rate
        String reference = "gw_" + ThreadLocalRandom.current().nextLong(100000, 999999);
        String response = success
                ? "APPROVED via " + method + " ref=" + reference
                : "DECLINED via " + method + " ref=" + reference + " reason=INSUFFICIENT_FUNDS";
        return new GatewayResult(success, response);
    }

    public record GatewayResult(boolean success, String rawResponse) {}
}
