-- Crear tabla para eventos de seguridad
CREATE TABLE IF NOT EXISTS eventos_seguridad (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    fecha TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    tipo VARCHAR(50) NOT NULL,
    usuario_id UUID REFERENCES usuarios(id),
    descripcion TEXT NOT NULL,
    detalles TEXT,
    
    CONSTRAINT eventos_seguridad_tipo_check 
        CHECK (tipo IN ('ACCESO', 'ROL', 'CONFIGURACION', 'SEGURIDAD'))
);

-- Crear índices para mejorar el rendimiento de las consultas
CREATE INDEX IF NOT EXISTS idx_eventos_seguridad_fecha ON eventos_seguridad(fecha);
CREATE INDEX IF NOT EXISTS idx_eventos_seguridad_tipo ON eventos_seguridad(tipo);
CREATE INDEX IF NOT EXISTS idx_eventos_seguridad_usuario ON eventos_seguridad(usuario_id);