package nic.meg.mcap.utils;

public final class Validator {

	private Validator() {
	}

	public static boolean isMobile(String input) {
		return input != null && input.matches("\\d{10,15}");
	}

	public static boolean isEmail(String input) {
		return input != null && input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}

	public static boolean isValidUsername(String input) {
		return input != null && input.matches("^[a-zA-Z0-9-]{6,50}$");
	}
	public static boolean isCountryCode(String input) {
	    return input != null && input.matches("^\\+\\d{1,4}$");
	}

	public static String normalizeMobile(String input) {
		if (input == null)
			return "";

		String digits = input.replaceAll("\\D", "");

		if (digits.length() >= 10) {
			return digits.substring(digits.length() - 10);
		}

		return digits;
	}

	public static String normalizeEmail(String input) {
		if (input == null)
			return "";
		return input.trim().toLowerCase();
	}

	public static String normalizeIdentifier(String input) {
		if (input == null)
			return "";

		input = input.trim();

		if (isEmail(input)) {
			return normalizeEmail(input);
		}

		return normalizeMobile(input);
	}
}