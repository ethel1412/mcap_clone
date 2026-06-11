package nic.meg.mcap.utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class EncryptionKeyProvider {

    @Value("${pii.encryption.key}")
    private String key;

    private static String secretKey;

    @PostConstruct
    public void init() {
        secretKey = key;
    }

    public static String getSecretKey() {
        return secretKey;
    }
}