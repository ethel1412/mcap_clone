package nic.meg.mcap.exception;

import org.springframework.http.HttpStatus;

public class InstituteNotFoundException extends PracticeCustomBaseException {

	public InstituteNotFoundException(String message) {
		super(HttpStatus.NOT_FOUND, message);
	}
}