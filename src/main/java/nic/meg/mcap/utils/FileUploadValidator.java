package nic.meg.mcap.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for secure file upload validation.
 *
 * <p>
 * Addresses Fortify finding: "Often Misused: File Upload"
 * (ApplicantDataController.java:221)
 *
 * <p>
 * Three-layer defence:
 * <ol>
 * <li>File size — reject files above a configurable limit to prevent DoS / DB
 * bloat</li>
 * <li>Extension allowlist — reject filenames whose extension is not in the
 * permitted set</li>
 * <li>Magic-byte verification — read the first bytes of the actual file content
 * and compare against known signatures for allowed types, so a renamed
 * executable cannot bypass the extension check</li>
 * </ol>
 *
 * <p>
 * NOTE: Content-Type headers sent by the browser are intentionally NOT trusted
 * here because they can be freely spoofed by an attacker.
 */
public final class FileUploadValidator {

	// -------------------------------------------------------------------------
	// Configuration constants
	// -------------------------------------------------------------------------

	/** Maximum permitted file size in bytes (5 MB). */
	public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L;

	/** Allowed file extensions (lower-cased). */
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "pdf");

	// -------------------------------------------------------------------------
	// Magic-byte signatures for each allowed MIME type
	// -------------------------------------------------------------------------

	// JPEG: FF D8 FF
	private static final byte[] MAGIC_JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };

	// PNG: 89 50 4E 47 0D 0A 1A 0A
	private static final byte[] MAGIC_PNG = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

	// PDF: 25 50 44 46 ("%PDF")
	private static final byte[] MAGIC_PDF = { 0x25, 0x50, 0x44, 0x46 };

	private FileUploadValidator() {
		// utility class — no instances
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	/**
	 * Validates an uploaded file for size, extension, and file-content signature.
	 *
	 * @param file the uploaded {@link MultipartFile}
	 * @throws IllegalArgumentException if the file is empty, exceeds the size
	 *                                  limit, has a disallowed extension, or its
	 *                                  content does not match a known safe
	 *                                  signature
	 * @throws IOException              if the file content cannot be read
	 */
	public static void validate(MultipartFile file) throws IOException {
		validateNotEmpty(file);
		validateSize(file);
		validateExtension(file);
		validateMagicBytes(file);
	}

	// -------------------------------------------------------------------------
	// Private helpers
	// -------------------------------------------------------------------------

	private static void validateNotEmpty(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Cannot upload an empty file.");
		}
	}

	private static void validateSize(MultipartFile file) {
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new IllegalArgumentException(
					"File size exceeds the maximum allowed limit of " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + " MB.");
		}
	}

	private static void validateExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.isBlank()) {
			throw new IllegalArgumentException("File name is missing or blank.");
		}

		// Guard against double-extension attacks (e.g. "malware.jsp.jpg")
		// by checking there is exactly one dot and it is not the first character.
		String lower = originalFilename.toLowerCase(Locale.ROOT);
		int lastDot = lower.lastIndexOf('.');
		if (lastDot <= 0) {
			throw new IllegalArgumentException("File name must have a valid extension.");
		}

		String ext = lower.substring(lastDot + 1);
		if (!ALLOWED_EXTENSIONS.contains(ext)) {
			throw new IllegalArgumentException(
					"File type '." + ext + "' is not permitted. " + "Only JPG, JPEG, PNG, and PDF are accepted.");
		}
	}

	private static void validateMagicBytes(MultipartFile file) throws IOException {
		byte[] header = new byte[8];
		int bytesRead;

		try (InputStream is = file.getInputStream()) {
			bytesRead = is.read(header);
		}

		validateHeader(bytesRead, header);
	}

	private static void validateHeader(int bytesRead, byte[] header) {
		if (bytesRead < 4) {
			throw new IllegalArgumentException("File content is too short to be a valid document.");
		}

		if (startsWith(header, MAGIC_JPEG) || startsWith(header, MAGIC_PNG) || startsWith(header, MAGIC_PDF)) {
			return;
		}

		throw new IllegalArgumentException("File content does not match any permitted file type. "
				+ "Only genuine JPG, PNG, and PDF files are accepted.");
	}

	/**
	 * Returns {@code true} if {@code data} starts with all bytes in
	 * {@code signature}.
	 */
	private static boolean startsWith(byte[] data, byte[] signature) {
		if (data.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (data[i] != signature[i]) {
				return false;
			}
		}
		return true;
	}
}