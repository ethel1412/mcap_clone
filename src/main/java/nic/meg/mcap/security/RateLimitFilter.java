package nic.meg.mcap.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int  MAX_REQUESTS = 10;
    private static final long WINDOW_MS    = 60_000L;

    private static final List<String> RATE_LIMITED_URIS = Arrays.asList(
            "/institute-form",
            "/submit",
            "/login",
            "/register",
            "/forgot-password",
            "/captcha/get-captcha"
    );

    private final Map<String, long[]> ipCounters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean isLimited = RATE_LIMITED_URIS.stream().anyMatch(uri::endsWith);

        if (isLimited) {
            String ip  = getClientIp(request);
            long   now = System.currentTimeMillis();

            ipCounters.compute(ip, (k, v) -> {
                if (v == null || (now - v[1]) > WINDOW_MS) {
                    return new long[]{1, now};
                }
                v[0]++;
                return v;
            });

            long[] counter = ipCounters.get(ip);
            if (counter != null && counter[0] > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write(
                        "Too many requests. Please wait a moment before trying again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
