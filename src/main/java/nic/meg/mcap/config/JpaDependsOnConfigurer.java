package nic.meg.mcap.config;

import nic.meg.mcap.utils.EncryptionKeyProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Map;

@Configuration
public class JpaDependsOnConfigurer {

    @Bean
    @DependsOn("encryptionKeyProvider")
    public HibernatePropertiesCustomizer jpaDependsOnCustomizer(EncryptionKeyProvider provider) {
        // This bean's only purpose is to enforce the @DependsOn annotation.
        // We don't need to return any actual properties.
        return (Map<String, Object> hibernateProperties) -> {};
    }
}