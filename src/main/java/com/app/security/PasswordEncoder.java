package com.app.security;

import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.util.encoders.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class PasswordEncoder {

    private static final int WORKLOAD = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = BCrypt.generate(
            plainPassword.getBytes(StandardCharsets.UTF_8),
            salt,
            WORKLOAD
        );

        // Formato: salt:hash
        return String.format("%s:%s",
            Base64.toBase64String(salt),
            Base64.toBase64String(hash)
        );
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.decode(parts[0]);
            byte[] hash = Base64.decode(parts[1]);

            byte[] testHash = BCrypt.generate(
                plainPassword.getBytes(StandardCharsets.UTF_8),
                salt,
                WORKLOAD
            );

            return java.util.Arrays.equals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean needsRehash(String hashedPassword) {
        return false; // Con este método siempre usamos WORKLOAD=12
    }
}
