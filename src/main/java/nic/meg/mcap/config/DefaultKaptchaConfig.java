package nic.meg.mcap.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

@Configuration
public class DefaultKaptchaConfig {

	@Bean
	public DefaultKaptcha getDefaultKaptcha() {
		DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
		Properties properties = new Properties();
		properties.setProperty("kaptcha.border", "yes");
		properties.setProperty("kaptcha.border.color", "red");
		properties.setProperty("kaptcha.textproducer.font.color", "blue");
		properties.setProperty("kaptcha.image.width", "300");
		properties.setProperty("kaptcha.image.height", "80");
		properties.setProperty("kaptcha.textproducer.font.size", "40");
		properties.setProperty("kaptcha.session.key", "captchaText");
		properties.setProperty("kaptcha.textproducer.char.length", "6");
		properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
		Config config = new Config(properties);
		defaultKaptcha.setConfig(config);
		return defaultKaptcha;
	}

}
