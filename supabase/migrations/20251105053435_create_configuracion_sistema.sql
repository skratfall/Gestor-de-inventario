-- Crear tabla configuracion_sistema
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    clave VARCHAR(50) NOT NULL UNIQUE,
    valor TEXT,
    tipo VARCHAR(20) NOT NULL DEFAULT 'string',
    descripcion TEXT,
    categoria VARCHAR(50) NOT NULL,
    editable_por_usuario BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES usuarios(id),
    updated_by UUID REFERENCES usuarios(id)
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_configuracion_clave ON configuracion_sistema(clave);
CREATE INDEX IF NOT EXISTS idx_configuracion_categoria ON configuracion_sistema(categoria);

-- Función para actualizar el updated_at
CREATE OR REPLACE FUNCTION update_configuracion_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar updated_at automáticamente
DROP TRIGGER IF EXISTS set_configuracion_updated_at ON configuracion_sistema;
CREATE TRIGGER set_configuracion_updated_at
    BEFORE UPDATE ON configuracion_sistema
    FOR EACH ROW
    EXECUTE FUNCTION update_configuracion_updated_at();

-- Habilitar RLS
ALTER TABLE configuracion_sistema ENABLE ROW LEVEL SECURITY;

-- Política para ver configuración (todos los usuarios autenticados)
CREATE POLICY configuracion_view_policy ON configuracion_sistema
    FOR SELECT
    USING (auth.uid() IS NOT NULL);

-- Política para modificar configuración (solo administradores)
CREATE POLICY configuracion_modify_policy ON configuracion_sistema
    FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM usuarios u
            JOIN roles r ON u.rol_id = r.id
            WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
        )
    );

-- Insertar configuraciones predeterminadas
INSERT INTO configuracion_sistema (clave, valor, tipo, descripcion, categoria, editable_por_usuario) 
VALUES 
    ('app.tema', 'claro', 'string', 'Tema de la aplicación', 'interfaz', true),
    ('app.idioma', 'es', 'string', 'Idioma de la aplicación', 'interfaz', true),
    ('app.nombre', 'Gestión de Inventario', 'string', 'Nombre de la aplicación', 'interfaz', false),
    ('db.url', '', 'string', 'URL de conexión a Supabase', 'conexion', false),
    ('db.apikey', '', 'string', 'API Key de Supabase', 'conexion', false),
    ('seguridad.2fa', 'false', 'boolean', 'Autenticación de dos factores', 'seguridad', true),
    ('seguridad.intentos_maximos', '3', 'number', 'Intentos máximos de login', 'seguridad', true),
    ('seguridad.tiempo_bloqueo', '30', 'number', 'Tiempo de bloqueo en minutos', 'seguridad', true),
    ('seguridad.auditar_accesos', 'true', 'boolean', 'Auditar accesos al sistema', 'seguridad', true),
    ('seguridad.auditar_roles', 'true', 'boolean', 'Auditar cambios en roles', 'seguridad', true),
    ('backup.automatico', 'true', 'boolean', 'Realizar respaldos automáticos', 'backup', true),
    ('backup.frecuencia', 'SEMANAL', 'string', 'Frecuencia de respaldos', 'backup', true)
ON CONFLICT (clave) DO NOTHING;

-- Comentarios en la tabla y columnas
COMMENT ON TABLE configuracion_sistema IS 'Configuraciones globales del sistema';
COMMENT ON COLUMN configuracion_sistema.clave IS 'Clave única de la configuración';
COMMENT ON COLUMN configuracion_sistema.valor IS 'Valor de la configuración';
COMMENT ON COLUMN configuracion_sistema.tipo IS 'Tipo de dato (string, number, boolean, json)';
COMMENT ON COLUMN configuracion_sistema.descripcion IS 'Descripción de la configuración';
COMMENT ON COLUMN configuracion_sistema.categoria IS 'Categoría de la configuración';
COMMENT ON COLUMN configuracion_sistema.editable_por_usuario IS 'Si la configuración puede ser modificada por usuarios';