package com.app.service;

import com.app.dao.RolDAO;
import com.app.dao.UsuarioDAOImpl;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.security.PasswordEncoder;
import com.app.security.SessionManager;

import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private static UsuarioService instance;
    private final UsuarioDAOImpl usuarioDAO;
    private final RolDAO rolDAO;
    private final SessionManager sessionManager;

    private UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.rolDAO = new RolDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    public static UsuarioService getInstance() {
        if (instance == null) {
            synchronized (UsuarioService.class) {
                if (instance == null) {
                    instance = new UsuarioService();
                }
            }
        }
        return instance;
    }

    public Usuario createUsuario(String username, String password, String email,
                                  String nombreCompleto, String rolNombre) {

        if (!sessionManager.hasPermission("usuarios", "create")) {
            throw new SecurityException("No tiene permiso para crear usuarios");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        if (usuarioDAO.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        Optional<Rol> rolOpt = rolDAO.findByNombre(rolNombre);
        if (!rolOpt.isPresent()) {
            throw new IllegalArgumentException("Rol no encontrado: " + rolNombre);
        }

        Rol rol = rolOpt.get();
        String passwordHash = PasswordEncoder.hashPassword(password);

        Usuario usuario = new Usuario(username, passwordHash, email, nombreCompleto, rol.getId());
        return usuarioDAO.save(usuario);
    }

    public Usuario updateUsuario(String userId, String username, String email,
                                  String nombreCompleto, String rolNombre, boolean activo) {

        if (!sessionManager.hasPermission("usuarios", "update")) {
            throw new SecurityException("No tiene permiso para actualizar usuarios");
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.findById(userId);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (username != null && !username.equals(usuario.getUsername())) {
            if (usuarioDAO.existsByUsername(username)) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }
            usuario.setUsername(username);
        }

        if (email != null) {
            usuario.setEmail(email);
        }

        if (nombreCompleto != null) {
            usuario.setNombreCompleto(nombreCompleto);
        }

        if (rolNombre != null) {
            Optional<Rol> rolOpt = rolDAO.findByNombre(rolNombre);
            if (!rolOpt.isPresent()) {
                throw new IllegalArgumentException("Rol no encontrado: " + rolNombre);
            }
            usuario.setRolId(rolOpt.get().getId());
        }

        usuario.setActivo(activo);

        return usuarioDAO.update(usuario);
    }

    public void deleteUsuario(String userId) {
        if (!sessionManager.hasPermission("usuarios", "delete")) {
            throw new SecurityException("No tiene permiso para eliminar usuarios");
        }

        Usuario currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("No puede eliminar su propio usuario");
        }

        usuarioDAO.delete(userId);
    }

    public Optional<Usuario> findUsuarioById(String userId) {
        if (!sessionManager.hasPermission("usuarios", "read")) {
            throw new SecurityException("No tiene permiso para ver usuarios");
        }

        return usuarioDAO.findById(userId);
    }

    public List<Usuario> findAllUsuarios() {
        if (!sessionManager.hasPermission("usuarios", "read")) {
            throw new SecurityException("No tiene permiso para ver usuarios");
        }

        return usuarioDAO.findAll();
    }

    public List<Usuario> findUsuariosByRol(String rolNombre) {
        if (!sessionManager.hasPermission("usuarios", "read")) {
            throw new SecurityException("No tiene permiso para ver usuarios");
        }

        return usuarioDAO.findByRol(rolNombre);
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        Usuario currentUser = sessionManager.getCurrentUser();

        if (currentUser == null || !currentUser.getId().equals(userId)) {
            if (!sessionManager.isAdmin()) {
                throw new SecurityException("Solo puede cambiar su propia contraseña");
            }
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.findById(userId);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!PasswordEncoder.verifyPassword(oldPassword, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        String newPasswordHash = PasswordEncoder.hashPassword(newPassword);
        usuarioDAO.updatePassword(userId, newPasswordHash);

        return true;
    }

    public void resetPassword(String userId, String newPassword) {
        if (!sessionManager.isAdmin()) {
            throw new SecurityException("Solo los administradores pueden restablecer contraseñas");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        String newPasswordHash = PasswordEncoder.hashPassword(newPassword);
        usuarioDAO.updatePassword(userId, newPasswordHash);
    }

    public void toggleUsuarioActivo(String userId) {
        if (!sessionManager.hasPermission("usuarios", "update")) {
            throw new SecurityException("No tiene permiso para modificar usuarios");
        }

        Usuario currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(userId)) {
            throw new IllegalArgumentException("No puede desactivar su propio usuario");
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.findById(userId);
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(!usuario.isActivo());
        usuarioDAO.update(usuario);
    }

    public boolean createUsuarioAsSystem(String username, String password, String email,
                                          String nombreCompleto, String rolId, boolean activo) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }

            if (password == null || password.length() < 6) {
                return false;
            }

            if (usuarioDAO.existsByUsername(username)) {
                return false;
            }

            String passwordHash = PasswordEncoder.hashPassword(password);
            Usuario usuario = new Usuario(username, passwordHash, email, nombreCompleto, rolId);
            usuario.setActivo(activo);

            Usuario saved = usuarioDAO.save(usuario);
            return saved != null;
        } catch (Exception e) {
            System.err.println("Error creating user as system: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasAnyUsuario() {
        try {
            List<Usuario> usuarios = usuarioDAO.findAllWithoutAuth();
            return usuarios != null && !usuarios.isEmpty();
        } catch (Exception e) {
            System.err.println("Error checking if any user exists: " + e.getMessage());
            return false;
        }
    }
}
