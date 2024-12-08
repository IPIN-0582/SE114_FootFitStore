package com.example.footfitstore.Utils;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.github.cdimascio.dotenv.Dotenv;

public class SignatureGenerator {
    public static String createPayOsSignature(long amount, String cancelUrl, String description, String orderCode, String returnUrl) {
        final String HMAC_SHA256 = "HmacSHA256";

        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();

        final String CHECKSUM_KEY = dotenv.get("PAYOS_CHECKSUM_KEY");

        try {
            String data = String.format(Locale.getDefault(), "amount=%d&cancelUrl=%s&description=%s&orderCode=%s&returnUrl=%s",
                    amount, cancelUrl, description, orderCode, returnUrl);

            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            sha256_HMAC.init(new SecretKeySpec(CHECKSUM_KEY.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));

            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

            Formatter formatter = new Formatter();
            for (byte b : hash) {
                formatter.format("%02x", b);
            }

            return formatter.toString();
        }
        catch (Exception e) {
            return null;
        }
    }
}
