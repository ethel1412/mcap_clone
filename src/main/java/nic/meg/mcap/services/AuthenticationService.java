package nic.meg.mcap.services;

import org.springframework.security.core.userdetails.UserDetails;

import nic.meg.mcap.enums.LoginType;

public interface AuthenticationService {

	Long getCurrentUserLoginActivityId();

	UserDetails isValidUser(String username, String password, String dob, LoginType loginType);
}