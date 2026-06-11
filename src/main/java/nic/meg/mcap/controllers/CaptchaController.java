package nic.meg.mcap.controllers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.kaptcha.impl.DefaultKaptcha;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/captcha")
public class CaptchaController {

	private final DefaultKaptcha defaultKaptcha;

	public CaptchaController(DefaultKaptcha defaultKaptcha) {
		this.defaultKaptcha = defaultKaptcha;
	}

	@PostMapping(value = "/get-captcha", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@ResponseBody
	public ResponseEntity<String> getCaptcha(HttpSession httpSession) throws IOException {

		String text = defaultKaptcha.createText();

		BufferedImage image = defaultKaptcha.createImage(text);

		httpSession.setAttribute("captchaText", text);

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

			ImageIO.write(image, "png", baos);

			String result = Base64.encodeBase64String(baos.toByteArray());

			return ResponseEntity.ok(result);
		}
	}
}