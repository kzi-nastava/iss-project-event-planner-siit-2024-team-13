package com.iss.eventorium.user.controllers;

import com.iss.eventorium.user.api.RoleApi;
import com.iss.eventorium.user.dtos.role.RoleDto;
import com.iss.eventorium.user.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController implements RoleApi {

    private final RoleService service;

    @GetMapping("/registration-options")
    public ResponseEntity<Collection<RoleDto>> getRegistrationRoles() {
        return ResponseEntity.ok(service.getRegistrationRoles());
    }
}
