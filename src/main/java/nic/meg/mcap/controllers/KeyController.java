package nic.meg.mcap.controllers;

import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nic.meg.mcap.utils.RSAUtil;

@Controller
@RequestMapping("/key")
public class KeyController {

	@Autowired
	private RSAUtil rsaUtil;

	@PostMapping(value = "/get-publickey", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	@ResponseBody
	public ResponseEntity<String> getPublickey() throws GeneralSecurityException {

		String publicKey = rsaUtil.generateKey();

		String pemKey = "-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----";

		return ResponseEntity.ok(pemKey);
	}

}
