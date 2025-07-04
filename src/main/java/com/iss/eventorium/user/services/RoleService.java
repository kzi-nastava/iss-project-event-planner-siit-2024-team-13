package com.iss.eventorium.user.services;

import com.iss.eventorium.user.dtos.role.RoleDto;
import com.iss.eventorium.user.mappers.RoleMapper;
import com.iss.eventorium.user.models.Role;
import com.iss.eventorium.user.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository repository;
    private final RoleMapper mapper;

    public Role findById(Long id) {
        return this.repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Role not found."));
    }

    public List<Role> findByName(String name) {
        return this.repository.findByName(name);
    }

    public List<RoleDto> getRegistrationRoles() {
        List<Role> roles = repository.findByNameIn(List.of("PROVIDER", "EVENT_ORGANIZER"));
        return roles.stream()
                .map(mapper::toResponse)
                .toList();
    }

}
