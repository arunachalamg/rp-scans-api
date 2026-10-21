package com.rpscans.api.config;

import com.rpscans.api.repository.StaffRepository;
import com.rpscans.api.repository.StaffPermissionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    // Maximum time before an already authenticated session is rechecked against the database.
    // Disabling an account or revoking login access may take up to 30 seconds to take effect.
    private static final long ACCESS_CHECK_INTERVAL_MS = 30_000L;
    private static final String LAST_ACCESS_CHECK = "rp.auth.lastAccessCheckMillis";

    private final StaffRepository staff;
    private final StaffPermissionRepository permissions;

    public AuthInterceptor(StaffRepository staff, StaffPermissionRepository permissions) {
        this.staff = staff;
        this.permissions = permissions;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("staffId") instanceof Long id)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }

        long now = System.currentTimeMillis();
        // Synchronize so simultaneous requests from one session do not all hit the database.
        synchronized (session) {
            Object last = session.getAttribute(LAST_ACCESS_CHECK);
            if (last instanceof Long lastChecked && now >= lastChecked
                    && now - lastChecked < ACCESS_CHECK_INTERVAL_MS) {
                return true;
            }

            boolean active = staff.findById(id).map(s -> s.active).orElse(false);
            boolean allowed = active && permissions.findById(id).map(p -> p.loginAccess).orElse(false);
            if (!allowed) {
                session.invalidate();
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
            }
            session.setAttribute(LAST_ACCESS_CHECK, System.currentTimeMillis());
        }
        return true;
    }
}
