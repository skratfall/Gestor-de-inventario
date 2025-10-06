package com.app.security;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordEncoder {

    private static final int BCRYPT_WORKLOAD = 12;

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean needsRehash(String hashedPassword) {
        try {
            String salt = hashedPassword.substring(0, 29);
            int workload = Integer.parseInt(salt.substring(4, 6));
            return workload < BCRYPT_WORKLOAD;
        } catch (Exception e) {
            return true;
        }
    }
}
