package com.app.service;

import com.app.dao.RolDAO;
import com.app.dao.UsuarioDAOImpl;
import com.app.model.Rol;
import com.app.model.Usuario;
import com.app.security.PasswordEncoder;
import com.app.security.SessionManager;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthenticationService {

    private static AuthenticationService instance;
    private final UsuarioDAOImpl usuarioDAO;
    private final RolDAO rolDAO;
    private final SessionManager sessionManager;

    private AuthenticationService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.rolDAO = new RolDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    public static AuthenticationService getInstance() {
        if (instance == null) {
            synchronized (AuthenticationService.class) {
                if (instance == null) {
                    instance = new AuthenticationService();
                }
            }
        }
        return instance;
    }

    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return false;
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.findByUsername(username.trim());

            if (!usuarioOpt.isPresent()) {
                return false;
            }

            Usuario usuario = usuarioOpt.get();

            if (!usuario.isActivo()) {
                return false;
            }

            if (!PasswordEncoder.verifyPassword(password, usuario.getPasswordHash())) {
                return false;
            }

            Optional<Rol> rolOpt = rolDAO.findById(usuario.getRolId());
            if (!rolOpt.isPresent()) {
                return false;
            }

            Rol rol = rolOpt.get();

            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioDAO.updateLastAccess(usuario.getId());

            sessionManager.startSession(usuario, rol);

            return true;

        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        sessionManager.endSession();
    }

    public boolean isAuthenticated() {
        return sessionManager.isSessionActive();
    }

    public Usuario getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    public Rol getCurrentRole() {
        return sessionManager.getCurrentRole();
    }

    public boolean hasPermission(String module, String action) {
        return sessionManager.hasPermission(module, action);
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.findById(userId);
            if (!usuarioOpt.isPresent()) {
                return false;
            }

            Usuario usuario = usuarioOpt.get();

            if (!PasswordEncoder.verifyPassword(oldPassword, usuario.getPasswordHash())) {
                return false;
            }

            String newPasswordHash = PasswordEncoder.hashPassword(newPassword);
            usuarioDAO.updatePassword(userId, newPasswordHash);

            return true;

        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }

    public boolean resetPassword(String userId, String newPassword) {
        if (!sessionManager.isAdmin()) {
            return false;
        }

        if (newPassword == null || newPassword.length() < 6) {
            return false;
        }

        try {
            String newPasswordHash = PasswordEncoder.hashPassword(newPassword);
            usuarioDAO.updatePassword(userId, newPasswordHash);
            return true;
        } catch (Exception e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }
}
