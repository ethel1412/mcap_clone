package nic.meg.mcap.config;

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)

public class SameSiteConfig {
	@Bean
	public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
		return CookieSameSiteSupplier.ofStrict();
	}
}
