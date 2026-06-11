package nic.meg.mcap.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String dob = request.getParameter("dob");
		CustomAuthenticationToken authRequest = new CustomAuthenticationToken(username, password, dob);

		return this.getAuthenticationManager().authenticate(authRequest);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		request.changeSessionId();

		SecurityContextHolder.getContext().setAuthentication(authResult);

		HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();

		repo.saveContext(SecurityContextHolder.getContext(), request, response);

		super.successfulAuthentication(request, response, chain, authResult);
	}
}