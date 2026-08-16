package com.ai.career.integration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class CredentialEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    public record EncryptedData(String ciphertext, String iv) {}

    private byte[] getMasterKeyBytes() {
        String envKey = System.getenv("APP_CREDENTIAL_ENCRYPTION_KEY");
        if (envKey == null || envKey.trim().isEmpty()) {
            // Fallback default test key if missing in dev environment
            envKey = "a1b2c3d4e5f678901234567890abcdef";
        }

        byte[] bytes = envKey.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Pad or hash to ensure exactly 32 bytes for AES-256
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            return padded;
        } else if (bytes.length > 32) {
            byte[] truncated = new byte[32];
            System.arraycopy(bytes, 0, truncated, 0, 32);
            return truncated;
        }
        return bytes;
    }

    public EncryptedData encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Plaintext payload cannot be null or empty");
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKey secretKey = new SecretKeySpec(getMasterKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return new EncryptedData(
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            log.error("Encryption failed for provider payload safely without leaking secrets");
            throw new IllegalStateException("Failed to encrypt integration payload safely", e);
        }
    }

    public String decrypt(String ciphertext, String ivBase64) {
        if (ciphertext == null || ivBase64 == null) {
            throw new IllegalArgumentException("Ciphertext and IV cannot be null");
        }
        try {
            byte[] cipherTextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] iv = Base64.getDecoder().decode(ivBase64);

            SecretKey secretKey = new SecretKeySpec(getMasterKeyBytes(), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for provider payload safely without leaking secrets");
            throw new IllegalStateException("Failed to decrypt integration payload safely", e);
        }
    }
}
