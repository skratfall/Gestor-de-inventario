-- Añadir campo nivel_acceso a la tabla roles
ALTER TABLE roles ADD COLUMN IF NOT EXISTS nivel_acceso INTEGER NOT NULL DEFAULT 1;

-- Actualizar los roles existentes con niveles de acceso apropiados
UPDATE roles SET nivel_acceso = CASE 
    WHEN nombre = 'ADMIN' THEN 4
    WHEN nombre = 'VENDEDOR' THEN 2
    WHEN nombre = 'USUARIO' THEN 1
    ELSE 1
END;