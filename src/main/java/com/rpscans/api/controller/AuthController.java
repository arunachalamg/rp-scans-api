package com.rpscans.api.controller;

import com.rpscans.api.entity.*;
import com.rpscans.api.repository.*;
import com.rpscans.api.service.PasswordHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final StaffRepository staffRepo;
    private final StaffPermissionRepository permissions;
    private final StaffCredentialRepository credentials;
    private final PasswordHasher hasher;

    public AuthController(StaffRepository s, StaffPermissionRepository p, StaffCredentialRepository c, PasswordHasher h) {
        staffRepo = s;
        permissions = p;
        credentials = c;
        hasher = h;
    }

    public record LoginRequest(String username, String password) {
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {
    }

    public record UserInfo(Long id, String staffId, String name, String role, String username) {
    }

    private UserInfo info(Staff s) {
        return new UserInfo(s.id, s.staffId, s.name, s.role, s.username);
    }

    private Staff validStaff(Long id) {
        Staff s = staffRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        if (!s.active || !permissions.findById(id).map(p -> p.loginAccess).orElse(false))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
        return s;
    }

    @PostMapping("/login")
    public UserInfo login(@RequestBody LoginRequest body, HttpServletRequest request) {
        if (body == null || body.username() == null || body.password() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password required");
        Staff s = staffRepo.findByUsernameIgnoreCase(body.username().trim()).orElse(null);
        StaffCredential c = s == null ? null : credentials.findById(s.id).orElse(null);
        if (s == null || c == null || !hasher.matches(body.password(), c.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        validStaff(s.id);
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("staffId", s.id);
        session.setMaxInactiveInterval(30 * 60);
        return info(s);
    }

    @GetMapping("/me")
    public UserInfo me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("staffId") instanceof Long id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        return info(validStaff(id));
    }

    @PostMapping("/logout")
    public Map<String, Boolean> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("ok", true);
    }

    @PostMapping("/change-password")
    public Map<String, Boolean> changePassword(@RequestBody ChangePasswordRequest body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("staffId") instanceof Long id))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        validStaff(id);
        StaffCredential c = credentials.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No password configured"));
        if (body == null || body.currentPassword() == null || !hasher.matches(body.currentPassword(), c.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect current password");
        if (body.newPassword() == null || body.newPassword().length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 12 characters");
        c.passwordHash = hasher.hash(body.newPassword());
        credentials.save(c);
        session.invalidate();
        return Map.of("ok", true);
    }
}
