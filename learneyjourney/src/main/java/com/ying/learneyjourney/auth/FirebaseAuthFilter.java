package com.ying.learneyjourney.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {
    @Override
    protected boolean shouldNotFilter(jakarta.servlet.http.HttpServletRequest req) {
        return "OPTIONS".equalsIgnoreCase(req.getMethod())
                || req.getRequestURI().startsWith("/public")
                || "/health".equals(req.getRequestURI())
                || req.getRequestURI().equals("/dbcheck");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
//        String path = req.getServletPath();
//        if (path.equals("/token") || path.equals("/ping") || path.equals("/health")) {
//            chain.doFilter(req, res);
//            return;
//        }
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            try {
                var decoded = FirebaseAuth.getInstance().verifyIdToken(h.substring(7), true);
                var auth = new UsernamePasswordAuthenticationToken(decoded.getUid(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (FirebaseAuthException e) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
