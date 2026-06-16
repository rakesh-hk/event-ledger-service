package com.example.accountservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String SPAN_HEADER = "X-Span-Id";
    private static final String REQUEST_HEADER = "X-Request-Id";

    @Value("${spring.application.name:unknown-service}")
    private String serviceName;

    @Value("${spring.profiles.active:local}")
    private String environment;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = resolveOrGenerate(request.getHeader(TRACE_HEADER));
            String spanId = resolveOrGenerate(request.getHeader(SPAN_HEADER));
            String requestId = resolveOrGenerate(request.getHeader(REQUEST_HEADER));

            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            MDC.put("requestId", requestId);
            MDC.put("serviceName", serviceName);
            MDC.put("environment", environment);

            response.setHeader(TRACE_HEADER, traceId);
            response.setHeader(SPAN_HEADER, spanId);
            response.setHeader(REQUEST_HEADER, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveOrGenerate(String headerValue) {
        return StringUtils.hasText(headerValue) ? headerValue : UUID.randomUUID().toString();
    }
}
