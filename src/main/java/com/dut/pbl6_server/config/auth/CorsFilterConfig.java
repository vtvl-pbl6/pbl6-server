package com.dut.pbl6_server.config.auth;

import com.dut.pbl6_server.common.util.CommonUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CorsFilterConfig implements Filter {

    @Value("${application.api-key.header-name}")
    private String apiKey;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Dynamically get the origin of the request
        String origin = request.getHeader("Origin");

        // Set the Access-Control-Allow-Origin header dynamically if the origin is present
        if (CommonUtils.String.isNotEmptyOrNull(origin))
            response.setHeader("Access-Control-Allow-Origin", origin);

        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Origin, Content-Type, Accept, " + apiKey);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Expose-Headers", "Location");

        // Handle pre-flight requests (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

