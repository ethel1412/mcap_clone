package nic.meg.mcap.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PasswordGenerator {

    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}|;:,.<>?"; // A set of common special characters

    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters for security.");
        }

        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Ensure the password has at least one of each required character type
        password.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        password.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));

        // Fill the remaining length with random characters from all pools
        String allChars = LOWERCASE_CHARS + UPPERCASE_CHARS + DIGITS + SPECIAL_CHARS;
        for (int i = 4; i < length; i++) { // Start from 4 because 4 chars are already added
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Shuffle the characters to ensure randomness of position
        List<Character> charList = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            charList.add(c);
        }
        Collections.shuffle(charList);

        StringBuilder shuffledPassword = new StringBuilder();
        for (char c : charList) {
            shuffledPassword.append(c);
        }

        return shuffledPassword.toString();
    }
}