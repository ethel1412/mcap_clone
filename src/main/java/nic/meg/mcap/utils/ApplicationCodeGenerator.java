package nic.meg.mcap.utils;

import java.time.Year;

public class ApplicationCodeGenerator {

	private static final String PREFIX = "APP";

	public static String generate(Short admissionId, Integer sequence) {

		if (admissionId == null || sequence == null) {
			throw new IllegalArgumentException("Invalid input values");
		}

		int year = Year.now().getValue() % 100;

		String paddedAdmissionId = String.format("%03d", admissionId);

		// supports large sequences safely
		String paddedSequence = String.format("%06d", sequence);

		// keep full sequence to avoid duplicates
		String combined = paddedAdmissionId + paddedSequence;

		String baseCode = String.format("%s%02d-%s", PREFIX, year, combined);

		String checksum = generateChecksum(baseCode);

		return baseCode + "-" + checksum;
	}

	private static String generateChecksum(String input) {

		int hash = Math.abs(input.hashCode());

		// 2-digit checksum
		return String.format("%02d", hash % 100);
	}
}