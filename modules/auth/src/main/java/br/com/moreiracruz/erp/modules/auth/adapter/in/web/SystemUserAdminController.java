package br.com.moreiracruz.erp.modules.auth.adapter.in.web;

import br.com.moreiracruz.erp.modules.auth.application.usecase.SystemUserAdminService;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.AdminUserResponse;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.CreateUserCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.ResetUserPasswordCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.UpdateUserRoleCommand;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/system/users")
public class SystemUserAdminController {

    private final SystemUserAdminService service;

    public SystemUserAdminController(SystemUserAdminService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public List<AdminUserResponse> listUsers() {
        return service.listUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse createUser(@RequestBody CreateUserCommand command) {
        return service.createUser(command);
    }

    @PutMapping("/{uuid}/role")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse updateRole(@PathVariable UUID uuid, @RequestBody UpdateUserRoleCommand command) {
        return service.updateRole(uuid, command);
    }

    @PutMapping("/{uuid}/password")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse resetPassword(@PathVariable UUID uuid, @RequestBody ResetUserPasswordCommand command) {
        return service.resetPassword(uuid, command);
    }

    @PostMapping("/{uuid}/activate")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse activate(@PathVariable UUID uuid) {
        return service.activate(uuid);
    }

    @PostMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse deactivate(@PathVariable UUID uuid, Authentication authentication) {
        return service.deactivate(uuid, (UUID) authentication.getPrincipal());
    }

    @PostMapping("/{uuid}/unlock")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public AdminUserResponse unlock(@PathVariable UUID uuid) {
        return service.unlock(uuid);
    }
}
