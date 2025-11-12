package com.app.dao;

import com.app.model.EventoSeguridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventoSeguridadDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(EventoSeguridadDAO.class);

    private Connection getConnection() throws SQLException {
        return SupabaseDatabaseConnection.getInstance().getConnection();
    }

    public EventoSeguridad save(EventoSeguridad evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser null");
        }

        String sql = "INSERT INTO eventos_seguridad (tipo, usuario_id, descripcion, detalles) " +
                    "VALUES (?, ?, ?, ?) RETURNING id, fecha";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getTipo());
            
            // Manejo de UUID: Convierte string a UUID si es válido, null si no
            if (evento.getUsuarioId() != null && !evento.getUsuarioId().equals("sistema")) {
                try {
                    stmt.setObject(2, java.util.UUID.fromString(evento.getUsuarioId()));
                } catch (IllegalArgumentException e) {
                    logger.warn("usuarioId no es un UUID válido: {}", evento.getUsuarioId());
                    stmt.setObject(2, null);
                }
            } else {
                stmt.setObject(2, null);
            }
            
            stmt.setString(3, evento.getDescripcion());
            stmt.setString(4, evento.getDetalles());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    evento.setId(rs.getString("id"));
                    evento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                }
            }
            logger.debug("Evento de seguridad guardado: {}", evento.getId());
            return evento;

        } catch (SQLException e) {
            logger.error("Error al guardar evento de seguridad: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar evento de seguridad", e);
        }
    }

    public List<EventoSeguridad> findAll() {
        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }
            logger.debug("Encontrados {} eventos de seguridad", eventos.size());

        } catch (SQLException e) {
            logger.error("Error al obtener eventos de seguridad: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos de seguridad", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo no puede ser null o vacío");
        }

        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE tipo = ? ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapResultSetToEvento(rs));
                }
            }
            logger.debug("Encontrados {} eventos de tipo {}", eventos.size(), tipo);

        } catch (SQLException e) {
            logger.error("Error al obtener eventos por tipo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos por tipo", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByFechaRange(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser null");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }

        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapResultSetToEvento(rs));
                }
            }
            logger.debug("Encontrados {} eventos en rango de fechas", eventos.size());

        } catch (SQLException e) {
            logger.error("Error al obtener eventos por rango de fecha: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos por rango de fecha", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByUsuarioId(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuarioId no puede ser null o vacío");
        }

        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE usuario_id = ?::uuid ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapResultSetToEvento(rs));
                }
            }
            logger.debug("Encontrados {} eventos para usuario {}", eventos.size(), usuarioId);

        } catch (SQLException e) {
            logger.error("Error al obtener eventos por usuario: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener eventos por usuario", e);
        }

        return eventos;
    }

    public Optional<EventoSeguridad> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID no puede ser null o vacío");
        }

        String sql = "SELECT * FROM eventos_seguridad WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    EventoSeguridad evento = mapResultSetToEvento(rs);
                    logger.debug("Evento encontrado: {}", id);
                    return Optional.of(evento);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener evento por ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener evento por ID", e);
        }

        logger.debug("Evento no encontrado: {}", id);
        return Optional.empty();
    }

    private EventoSeguridad mapResultSetToEvento(ResultSet rs) throws SQLException {
        EventoSeguridad evento = new EventoSeguridad();
        evento.setId(rs.getString("id"));
        evento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        evento.setTipo(rs.getString("tipo"));
        evento.setUsuarioId(rs.getString("usuario_id"));
        evento.setDescripcion(rs.getString("descripcion"));
        evento.setDetalles(rs.getString("detalles"));
        return evento;
    }
}