// src/main/java/nic/meg/mcap/utils/SecurityConstants.java
package nic.meg.mcap.utils;

public class SecurityConstants {
	// UPDATED: PASSWORD_REGEX to match the full set of special characters from
	// PasswordGenerator.
	// Password must be:
	// - 8 to 50 characters long.
	// - Contain at least one digit (0-9).
	// - Contain at least one lowercase letter (a-z).
	// - Contain at least one uppercase letter (A-Z).
	// - Contain at least one special character from the set:
	// !@#$%^&*()-_=+[]{}|;:,.<>?
	// - NOT contain any whitespace characters.

	public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_\\-=\\[\\]{}|;:,.<>\\?])(?=\\S+$).{8,50}$";

	public static final String USERNAME_REGEX = "^[a-zA-Z0-9-]{6,20}$";

	public static final String MOBILE_REGEX = "^\\+?[1-9]\\d{7,15}$";

	public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
}