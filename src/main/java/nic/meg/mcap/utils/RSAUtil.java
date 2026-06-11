package nic.meg.mcap.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RSAUtil {

	@Value("${app.private-key}")
	private String privateKeyString;

	@Value("${app.public-key}")
	private String publicKeyString;

	public String decrypt(String encryptedMessage)
			throws java.security.GeneralSecurityException, IllegalArgumentException {

		byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);

		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");

		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)), StandardCharsets.UTF_8);
	}

	public String generateKey() {
		return publicKeyString;
	}
}