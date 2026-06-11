package nic.meg.mcap.services;

import nic.meg.mcap.dto.request.OTPRequestDTO;
import nic.meg.mcap.dto.response.SmsResponse;

public interface OtpService {

	SmsResponse generateOtp(OTPRequestDTO otpDTO);

	boolean verifyOtp(OTPRequestDTO otpDTO);

	void clearOtp(String mobile);
}
