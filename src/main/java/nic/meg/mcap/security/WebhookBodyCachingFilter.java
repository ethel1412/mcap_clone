package nic.meg.mcap.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Wraps the incoming request in a ContentCachingRequestWrapper ONLY for the
 * Cashfree webhook endpoint.
 *
 * Cashfree's HMAC signature is computed over the raw request body bytes.
 * Spring's InputStream can only be read once — after the first read it returns
 * empty. ContentCachingRequestWrapper buffers the bytes so both the signature
 * verifier and the JSON parser can access the same body.
 */
@Component
public class WebhookBodyCachingFilter extends OncePerRequestFilter {

    private static final String WEBHOOK_PATH = "/applicants/payment/webhook";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals(WEBHOOK_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request);

        // Eagerly read the body into the cache BEFORE the filter chain runs.
        // ContentCachingRequestWrapper is lazy — if not read here, the cache
        // will be empty when the controller reads it.
        wrapper.getInputStream().readAllBytes();

        filterChain.doFilter(wrapper, response);
    }
}