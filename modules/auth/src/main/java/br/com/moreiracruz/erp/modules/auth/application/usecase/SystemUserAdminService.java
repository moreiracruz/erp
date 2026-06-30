package br.com.moreiracruz.erp.modules.auth.application.usecase;

import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.model.UsuarioStatus;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.AdminUserResponse;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.CreateUserCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.ResetUserPasswordCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.in.UpdateUserRoleCommand;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SystemUserAdminService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public SystemUserAdminService(UsuarioRepository usuarioRepository,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getUsername))
                .map(this::toResponse)
                .toList();
    }

    public AdminUserResponse createUser(CreateUserCommand command, Role actorRole) {
        String username = normalizeUsername(command.username());
        validatePassword(command.password());
        Role role = parseRole(command.role());
        assertCanAssignRole(role, actorRole);
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("Usuário já cadastrado");
        }
        Usuario usuario = Usuario.create(username, passwordEncoder.encode(command.password()), role);
        return toResponse(usuarioRepository.save(usuario));
    }

    public AdminUserResponse updateRole(UUID uuid, UpdateUserRoleCommand command, Role actorRole) {
        Usuario usuario = findUser(uuid);
        assertCanManageUser(usuario, actorRole);
        Role newRole = parseRole(command.role());
        assertCanAssignRole(newRole, actorRole);
        usuario.changeRole(newRole);
        return toResponse(usuarioRepository.save(usuario));
    }

    public AdminUserResponse resetPassword(UUID uuid, ResetUserPasswordCommand command, Role actorRole) {
        validatePassword(command.password());
        Usuario usuario = findUser(uuid);
        assertCanManageUser(usuario, actorRole);
        usuario.changePasswordHash(passwordEncoder.encode(command.password()));
        return toResponse(usuarioRepository.save(usuario));
    }

    public AdminUserResponse activate(UUID uuid, Role actorRole) {
        Usuario usuario = findUser(uuid);
        assertCanManageUser(usuario, actorRole);
        usuario.setActive(true);
        return toResponse(usuarioRepository.save(usuario));
    }

    public AdminUserResponse deactivate(UUID uuid, UUID actorUuid, Role actorRole) {
        if (uuid.equals(actorUuid)) {
            throw new ValidationException("Usuário não pode desativar a própria conta");
        }
        Usuario usuario = findUser(uuid);
        assertCanManageUser(usuario, actorRole);
        if (usuario.getRole() == Role.ROLE_SUPER_ADMIN
                && usuarioRepository.countByRoleAndStatus(Role.ROLE_SUPER_ADMIN, UsuarioStatus.ACTIVE) <= 1) {
            throw new ValidationException("Último super admin ativo não pode ser desativado");
        }
        usuario.setActive(false);
        return toResponse(usuarioRepository.save(usuario));
    }

    public AdminUserResponse unlock(UUID uuid, Role actorRole) {
        Usuario usuario = findUser(uuid);
        assertCanManageUser(usuario, actorRole);
        usuario.resetLockout();
        return toResponse(usuarioRepository.save(usuario));
    }

    private Usuario findUser(UUID uuid) {
        return usuarioRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + uuid));
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new ValidationException("Perfil do usuário é obrigatório");
        }
        try {
            return Role.valueOf(role.trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Perfil do usuário inválido");
        }
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank() || username.length() > 255) {
            throw new ValidationException("Usuário deve ter entre 1 e 255 caracteres");
        }
        return username.trim().toLowerCase();
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Senha deve ter pelo menos 8 caracteres");
        }
    }

    private void assertCanAssignRole(Role role, Role actorRole) {
        if (role == Role.ROLE_SUPER_ADMIN && actorRole != Role.ROLE_SUPER_ADMIN) {
            throw new ValidationException("Apenas super admin pode atribuir ROLE_SUPER_ADMIN");
        }
    }

    private void assertCanManageUser(Usuario usuario, Role actorRole) {
        if (usuario.getRole() == Role.ROLE_SUPER_ADMIN && actorRole != Role.ROLE_SUPER_ADMIN) {
            throw new ValidationException("Apenas super admin pode alterar outro super admin");
        }
    }

    private AdminUserResponse toResponse(Usuario usuario) {
        return new AdminUserResponse(
                usuario.getUuid(),
                usuario.getUsername(),
                usuario.getRole().name(),
                usuario.getStatus().name(),
                usuario.isActive(),
                usuario.getFailedAttempts(),
                usuario.getLockedUntil(),
                usuario.getCreatedAt());
    }
}
