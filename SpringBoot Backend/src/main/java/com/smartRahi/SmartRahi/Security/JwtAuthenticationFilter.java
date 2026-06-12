package com.smartRahi.SmartRahi.Security;

// Filters incoming requests to validate JWT tokens and set authentication in the security context, skipping /api/auth/ endpoints.
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // ✅ Skip JWT check for auth endpoints
        if (req.getServletPath().startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        final String authHeader = req.getHeader("Authorization");
        final String token;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        token = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            chain.doFilter(req, res);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails ud = userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(token, ud)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        ud, null, ud.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        chain.doFilter(req, res);
    }

}
