package com.rpscans.api.controller;

import com.rpscans.api.entity.Staff;
import com.rpscans.api.entity.StaffCredential;
import com.rpscans.api.repository.StaffRepository;
import com.rpscans.api.repository.StaffCredentialRepository;
import com.rpscans.api.service.PasswordHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/staff")
public class StaffPasswordController {
    private final StaffRepository staff;
    private final StaffCredentialRepository credentials;
    private final PasswordHasher hasher;

    public StaffPasswordController(StaffRepository staff, StaffCredentialRepository credentials, PasswordHasher hasher) {
        this.staff = staff;
        this.credentials = credentials;
        this.hasher = hasher;
    }

    public record PasswordRequest(String newPassword) {}

    @PutMapping("/{staffId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPassword(@PathVariable Long staffId, @RequestBody PasswordRequest body, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("staffId") instanceof Long actorId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        Staff actor = staff.findById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required"));
        // Authorization is enforced on the server, never based on the role supplied by the browser.
        if (!actor.active || !"Admin".equalsIgnoreCase(actor.role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator access required");
        Staff target = staff.findById(staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found"));
        if (target.username == null || target.username.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set a username before assigning a password");
        if (body == null || body.newPassword() == null || body.newPassword().length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 12 characters");
        StaffCredential credential = credentials.findById(staffId).orElseGet(() -> {
            StaffCredential row = new StaffCredential();
            row.staffId = staffId;
            return row;
        });
        credential.passwordHash = hasher.hash(body.newPassword());
        credentials.save(credential);
    }
}
