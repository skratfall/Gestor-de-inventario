package com.app.dao;

import com.app.model.EventoSeguridad;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventoSeguridadDAO {
    
    private Connection getConnection() throws SQLException {
        return SupabaseDatabaseConnection.getInstance().getConnection();
    }

    public EventoSeguridad save(EventoSeguridad evento) {
        String sql = "INSERT INTO eventos_seguridad (tipo, usuario_id, descripcion, detalles) " +
                    "VALUES (?, ?, ?, ?) RETURNING id, fecha";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getTipo());
            stmt.setString(2, evento.getUsuarioId());
            stmt.setString(3, evento.getDescripcion());
            stmt.setString(4, evento.getDetalles());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                evento.setId(rs.getString("id"));
                evento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
            return evento;

        } catch (SQLException e) {
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

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener eventos de seguridad", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByTipo(String tipo) {
        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE tipo = ? ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener eventos por tipo", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByFechaRange(LocalDateTime desde, LocalDateTime hasta) {
        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE fecha BETWEEN ? AND ? ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener eventos por rango de fecha", e);
        }

        return eventos;
    }

    public List<EventoSeguridad> findByUsuarioId(String usuarioId) {
        List<EventoSeguridad> eventos = new ArrayList<>();
        String sql = "SELECT * FROM eventos_seguridad WHERE usuario_id = ?::uuid ORDER BY fecha DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                eventos.add(mapResultSetToEvento(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener eventos por usuario", e);
        }

        return eventos;
    }

    public Optional<EventoSeguridad> findById(String id) {
        String sql = "SELECT * FROM eventos_seguridad WHERE id = ?::uuid";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToEvento(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener evento por ID", e);
        }

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