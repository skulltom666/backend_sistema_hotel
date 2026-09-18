package com.hotel.sistemahotelero.security.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @Value("${app.tenant.header:X-Tenant-ID}")
    private String tenantHeader;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Si es una ruta pública, no validamos tenant
        if (request.getRequestURI().contains("/auth/") ||
                request.getRequestURI().contains("/public/") ||
                request.getRequestURI().contains("/actuator/")) {
            return true;
        }

        // 1) El tenant ya viene en el token JWT (fuente de verdad).
        // 2) Solo como respaldo para clientes que envían el header, lo usamos.
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = request.getHeader(tenantHeader);
            if (tenantId != null && !tenantId.isEmpty()) {
                TenantContext.setCurrentTenant(tenantId);
                log.debug("Tenant set from header: {}", tenantId);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}