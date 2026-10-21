package com.rpscans.api.controller;

import com.rpscans.api.entity.Staff;
import com.rpscans.api.repository.StaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private final StaffRepository repository;
    public StaffController(StaffRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Staff> list() { return repository.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Staff create(@RequestBody Staff input) {
        input.id = null;
        normalize(input);
        validate(input);
        if (input.staffId == null || input.staffId.isBlank()) {
            input.staffId = nextCode();
        }
        if (repository.existsByStaffId(input.staffId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Staff ID already exists");
        }
        return repository.save(input);
    }

    @PutMapping("/{id}")
    public Staff update(@PathVariable Long id, @RequestBody Staff input) {
        Staff staff = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        normalize(input);
        validate(input);
        if (input.staffId == null || input.staffId.isBlank()) input.staffId = staff.staffId;
        staff.staffId = input.staffId;
        staff.name = input.name;
        staff.role = input.role;
        staff.phone = input.phone;
        staff.email = input.email;
        staff.address = input.address;
        staff.username = input.username;
        staff.active = input.active;
        return repository.save(staff);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }

    private static void normalize(Staff input) {
        if (input.username != null && input.username.isBlank()) input.username = null;
        if (input.email != null && input.email.isBlank()) input.email = null;
    }

    private static void validate(Staff input) {
        if (input.name == null || input.name.isBlank() || input.role == null || input.role.isBlank()
                || input.phone == null || input.phone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, role and phone required");
        }
    }

    // Generate ID based on database sequence to avoid duplicate IDs on concurrent creates.
    // For production, prefer a database-backed sequence or unique-ID service.
    private String nextCode() {
        return "ST" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
