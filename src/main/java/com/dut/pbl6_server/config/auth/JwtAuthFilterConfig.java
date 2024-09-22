package com.dut.pbl6_server.config.auth;

import com.dut.pbl6_server.common.constant.CommonConstants;
import com.dut.pbl6_server.common.constant.ErrorMessageConstants;
import com.dut.pbl6_server.common.exception.ForbiddenException;
import com.dut.pbl6_server.common.exception.UnauthorizedException;
import com.dut.pbl6_server.common.model.AbstractResponse;
import com.dut.pbl6_server.common.model.ErrorResponse;
import com.dut.pbl6_server.common.util.CommonUtils;
import com.dut.pbl6_server.common.util.ErrorUtils;
import com.dut.pbl6_server.entity.Account;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthFilterConfig extends OncePerRequestFilter {
    @Value("${application.api-key.header-name}")
    private String apiKeyHeader;
    @Value("${application.api-key.header-value}")
    private String apiKeyValue;

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Allow if the request is from web socket
            if (request.getRequestURI().startsWith("/ws")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check API key
            final String apiKey = request.getHeader(apiKeyHeader);
            if (CommonUtils.String.isEmptyOrNull(apiKey))
                throw new UnauthorizedException(ErrorMessageConstants.API_KEY_IS_REQUIRED);
            if (!apiKey.equals(apiKeyValue))
                throw new ForbiddenException(ErrorMessageConstants.API_KAY_NOT_MATCH);

            // Jwt token
            final String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith(CommonConstants.JWT_TYPE)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Get the token from the header
            String token = authorizationHeader.replaceFirst("%s ".formatted(CommonConstants.JWT_TYPE), "");

            // Set new authentication object to the SecurityContextHolder
            Account userDetails = jwtUtils.getAccountFromToken(token);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);
        } catch (ForbiddenException | UnauthorizedException ex) {
            response.setStatus(ex instanceof UnauthorizedException ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ErrorResponse error = ErrorUtils.getExceptionError(ex.getMessage());
            AbstractResponse abstractResponse = AbstractResponse.error(error);
            response
                .getWriter()
                .write(Objects.requireNonNull(CommonUtils.Json.encode(abstractResponse)));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ErrorResponse error = new ErrorResponse(ErrorMessageConstants.INTERNAL_SERVER_ERROR, ex.getMessage());
            AbstractResponse abstractResponse = AbstractResponse.error(error);
            response
                .getWriter()
                .write(Objects.requireNonNull(CommonUtils.Json.encode(abstractResponse)));
        }
    }
}
