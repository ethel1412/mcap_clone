package nic.meg.mcap.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.AEADBadTagException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class StringCryptoConverter implements AttributeConverter<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(StringCryptoConverter.class);

    // ✅ Modern secure algorithm
    private static final String AES_GCM = "AES/GCM/NoPadding";

    // ⚠️ Legacy (for backward compatibility only)
    private static final String AES_ECB = "AES/ECB/PKCS5Padding";

    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_LENGTH = 12;       // bytes (recommended for GCM)

    private final Key key;

    public StringCryptoConverter() {
        try {
            String secret = EncryptionKeyProvider.getSecretKey();
            if (secret == null || secret.length() < 16) {
                throw new IllegalStateException("Invalid AES key. Must be at least 16 chars.");
            }
            this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize StringCryptoConverter", e);
        }
    }

    // =========================
    // Encrypt (GCM)
    // =========================
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;

        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // store IV + ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (Exception e) {
            logger.error("Encryption failed. Returning original value.", e);
            return attribute; // fallback (your existing behavior)
        }
    }

    // =========================
    // Decrypt (GCM → fallback ECB)
    // =========================
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            // If data is too small, it’s likely old/plain
            if (decoded.length < IV_LENGTH) {
                return dbData;
            }

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (AEADBadTagException gcmFail) {
            // 🔁 fallback for old ECB-encrypted data
            try {
                Cipher legacyCipher = Cipher.getInstance(AES_ECB);
                legacyCipher.init(Cipher.DECRYPT_MODE, key);
                return new String(
                        legacyCipher.doFinal(Base64.getDecoder().decode(dbData)),
                        StandardCharsets.UTF_8
                );
            } catch (Exception ex) {
                logger.warn("Fallback decryption failed. Returning raw data.");
                return dbData;
            }

        } catch (Exception e) {
            // Non-encrypted / corrupt / plain text
            return dbData;
        }
    }
}