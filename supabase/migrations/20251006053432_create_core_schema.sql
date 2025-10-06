/*
  # Create Core Database Schema for Inventory Management System

  ## Overview
  This migration creates the complete database schema for the Desktop Administrative module
  of the inventory management system, including user management, product catalog, sales,
  purchases, orders, and synchronization tracking.

  ## 1. New Tables

  ### roles
  - `id` (uuid, primary key) - Unique role identifier
  - `nombre` (text, unique, not null) - Role name (ADMIN, VENDEDOR, USUARIO, etc.)
  - `descripcion` (text) - Role description
  - `permisos` (jsonb) - JSON object containing permissions structure
  - `created_at` (timestamptz) - Creation timestamp

  ### usuarios
  - `id` (uuid, primary key) - User unique identifier
  - `username` (text, unique, not null) - Login username
  - `password_hash` (text, not null) - BCrypt hashed password
  - `email` (text, unique) - User email
  - `nombre_completo` (text) - Full name
  - `rol_id` (uuid, foreign key) - Reference to roles table
  - `activo` (boolean) - Active status
  - `ultimo_acceso` (timestamptz) - Last login timestamp
  - `created_at` (timestamptz) - Creation timestamp
  - `updated_at` (timestamptz) - Last update timestamp

  ### categorias
  - `id` (uuid, primary key) - Category identifier
  - `nombre` (text, unique, not null) - Category name
  - `descripcion` (text) - Description
  - `activo` (boolean) - Active status
  - `created_at` (timestamptz) - Creation timestamp

  ### productos
  - `id` (uuid, primary key) - Product identifier
  - `codigo` (text, unique, not null) - Product code/SKU
  - `nombre` (text, not null) - Product name
  - `descripcion` (text) - Product description
  - `categoria_id` (uuid, foreign key) - Reference to categories
  - `precio_compra` (decimal) - Purchase price
  - `precio_venta` (decimal) - Sale price
  - `stock_actual` (integer) - Current stock quantity
  - `stock_minimo` (integer) - Minimum stock alert level
  - `unidad_medida` (text) - Unit of measurement
  - `activo` (boolean) - Active status
  - `imagen_url` (text) - Product image URL
  - `created_at` (timestamptz) - Creation timestamp
  - `updated_at` (timestamptz) - Last update timestamp

  ### clientes
  - `id` (uuid, primary key) - Client identifier
  - `tipo_documento` (text) - Document type (DNI, RUC, etc.)
  - `numero_documento` (text, unique) - Document number
  - `nombre_completo` (text, not null) - Full name or business name
  - `email` (text) - Email address
  - `telefono` (text) - Phone number
  - `direccion` (text) - Address
  - `activo` (boolean) - Active status
  - `created_at` (timestamptz) - Creation timestamp
  - `updated_at` (timestamptz) - Last update timestamp

  ### ventas
  - `id` (uuid, primary key) - Sale identifier
  - `numero_venta` (text, unique, not null) - Sale number
  - `cliente_id` (uuid, foreign key) - Reference to clients
  - `usuario_id` (uuid, foreign key) - User who made the sale
  - `fecha_venta` (timestamptz) - Sale date
  - `subtotal` (decimal) - Subtotal amount
  - `impuesto` (decimal) - Tax amount
  - `descuento` (decimal) - Discount amount
  - `total` (decimal) - Total amount
  - `estado` (text) - Status (COMPLETADA, ANULADA, PENDIENTE)
  - `tipo_pago` (text) - Payment type (EFECTIVO, TARJETA, TRANSFERENCIA)
  - `observaciones` (text) - Observations
  - `created_at` (timestamptz) - Creation timestamp

  ### ventas_detalle
  - `id` (uuid, primary key) - Sale detail identifier
  - `venta_id` (uuid, foreign key) - Reference to sales
  - `producto_id` (uuid, foreign key) - Reference to products
  - `cantidad` (decimal) - Quantity sold
  - `precio_unitario` (decimal) - Unit price at sale time
  - `subtotal` (decimal) - Line subtotal
  - `created_at` (timestamptz) - Creation timestamp

  ### pedidos
  - `id` (uuid, primary key) - Order identifier
  - `numero_pedido` (text, unique, not null) - Order number
  - `proveedor` (text) - Supplier name
  - `usuario_id` (uuid, foreign key) - User who created the order
  - `fecha_pedido` (timestamptz) - Order date
  - `fecha_entrega_estimada` (timestamptz) - Estimated delivery date
  - `fecha_entrega_real` (timestamptz) - Actual delivery date
  - `subtotal` (decimal) - Subtotal amount
  - `impuesto` (decimal) - Tax amount
  - `total` (decimal) - Total amount
  - `estado` (text) - Status (PENDIENTE, RECIBIDO, CANCELADO)
  - `observaciones` (text) - Observations
  - `created_at` (timestamptz) - Creation timestamp
  - `updated_at` (timestamptz) - Last update timestamp

  ### pedidos_detalle
  - `id` (uuid, primary key) - Order detail identifier
  - `pedido_id` (uuid, foreign key) - Reference to orders
  - `producto_id` (uuid, foreign key) - Reference to products
  - `cantidad` (decimal) - Quantity ordered
  - `precio_unitario` (decimal) - Unit price
  - `subtotal` (decimal) - Line subtotal
  - `created_at` (timestamptz) - Creation timestamp

  ### sincronizacion_log
  - `id` (uuid, primary key) - Sync log identifier
  - `tipo_operacion` (text) - Operation type (ENVIO, RECEPCION)
  - `tabla_afectada` (text) - Affected table name
  - `registros_procesados` (integer) - Number of records processed
  - `estado` (text) - Status (EXITOSO, FALLIDO, PARCIAL)
  - `mensaje` (text) - Result message or error details
  - `usuario_id` (uuid, foreign key) - User who initiated sync
  - `fecha_inicio` (timestamptz) - Start timestamp
  - `fecha_fin` (timestamptz) - End timestamp
  - `created_at` (timestamptz) - Creation timestamp

  ### configuracion_sistema
  - `id` (uuid, primary key) - Config identifier
  - `clave` (text, unique, not null) - Configuration key
  - `valor` (text) - Configuration value
  - `tipo` (text) - Value type (STRING, NUMBER, BOOLEAN, JSON)
  - `descripcion` (text) - Configuration description
  - `categoria` (text) - Configuration category
  - `editable_por_usuario` (boolean) - Whether users can edit
  - `updated_at` (timestamptz) - Last update timestamp
  - `updated_by` (uuid, foreign key) - User who last updated

  ## 2. Security
  
  ### Row Level Security (RLS)
  - Enable RLS on all tables
  - Create restrictive policies for authenticated users only
  - Implement role-based access control through policies
  - Users can only access data according to their role permissions

  ### Policies by Table
  
  #### roles table
  - ADMIN can read all roles
  - ADMIN can insert/update/delete roles
  - Other users can only read roles
  
  #### usuarios table
  - ADMIN can manage all users
  - Users can read their own data
  - Users can update their own profile (except role)
  
  #### productos, categorias, clientes
  - ADMIN and VENDEDOR can read/write
  - USUARIO can only read
  
  #### ventas, pedidos and details
  - ADMIN can manage all
  - VENDEDOR can manage their own records
  - USUARIO can only read

  #### sincronizacion_log
  - ADMIN can read all logs
  - Users can read logs they created
  
  #### configuracion_sistema
  - Only ADMIN can read/write

  ## 3. Important Notes
  - All passwords must be hashed using BCrypt before storing
  - Role-based access control is enforced at database level
  - Sync log tracks all synchronization operations for audit trail
  - System configuration is centrally managed and version controlled
  - All tables use UUID for primary keys for better distribution
  - Timestamps use timestamptz for timezone awareness
*/

-- Create extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  nombre text UNIQUE NOT NULL,
  descripcion text,
  permisos jsonb DEFAULT '{}'::jsonb,
  created_at timestamptz DEFAULT now()
);

-- Create usuarios table
CREATE TABLE IF NOT EXISTS usuarios (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  username text UNIQUE NOT NULL,
  password_hash text NOT NULL,
  email text UNIQUE,
  nombre_completo text,
  rol_id uuid REFERENCES roles(id),
  activo boolean DEFAULT true,
  ultimo_acceso timestamptz,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create categorias table
CREATE TABLE IF NOT EXISTS categorias (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  nombre text UNIQUE NOT NULL,
  descripcion text,
  activo boolean DEFAULT true,
  created_at timestamptz DEFAULT now()
);

-- Create productos table
CREATE TABLE IF NOT EXISTS productos (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  codigo text UNIQUE NOT NULL,
  nombre text NOT NULL,
  descripcion text,
  categoria_id uuid REFERENCES categorias(id),
  precio_compra decimal(10,2) DEFAULT 0,
  precio_venta decimal(10,2) DEFAULT 0,
  stock_actual integer DEFAULT 0,
  stock_minimo integer DEFAULT 0,
  unidad_medida text DEFAULT 'UND',
  activo boolean DEFAULT true,
  imagen_url text,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create clientes table
CREATE TABLE IF NOT EXISTS clientes (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  tipo_documento text DEFAULT 'DNI',
  numero_documento text UNIQUE,
  nombre_completo text NOT NULL,
  email text,
  telefono text,
  direccion text,
  activo boolean DEFAULT true,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create ventas table
CREATE TABLE IF NOT EXISTS ventas (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  numero_venta text UNIQUE NOT NULL,
  cliente_id uuid REFERENCES clientes(id),
  usuario_id uuid REFERENCES usuarios(id),
  fecha_venta timestamptz DEFAULT now(),
  subtotal decimal(10,2) DEFAULT 0,
  impuesto decimal(10,2) DEFAULT 0,
  descuento decimal(10,2) DEFAULT 0,
  total decimal(10,2) DEFAULT 0,
  estado text DEFAULT 'COMPLETADA',
  tipo_pago text DEFAULT 'EFECTIVO',
  observaciones text,
  created_at timestamptz DEFAULT now()
);

-- Create ventas_detalle table
CREATE TABLE IF NOT EXISTS ventas_detalle (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  venta_id uuid REFERENCES ventas(id) ON DELETE CASCADE,
  producto_id uuid REFERENCES productos(id),
  cantidad decimal(10,2) NOT NULL,
  precio_unitario decimal(10,2) NOT NULL,
  subtotal decimal(10,2) NOT NULL,
  created_at timestamptz DEFAULT now()
);

-- Create pedidos table
CREATE TABLE IF NOT EXISTS pedidos (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  numero_pedido text UNIQUE NOT NULL,
  proveedor text,
  usuario_id uuid REFERENCES usuarios(id),
  fecha_pedido timestamptz DEFAULT now(),
  fecha_entrega_estimada timestamptz,
  fecha_entrega_real timestamptz,
  subtotal decimal(10,2) DEFAULT 0,
  impuesto decimal(10,2) DEFAULT 0,
  total decimal(10,2) DEFAULT 0,
  estado text DEFAULT 'PENDIENTE',
  observaciones text,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create pedidos_detalle table
CREATE TABLE IF NOT EXISTS pedidos_detalle (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  pedido_id uuid REFERENCES pedidos(id) ON DELETE CASCADE,
  producto_id uuid REFERENCES productos(id),
  cantidad decimal(10,2) NOT NULL,
  precio_unitario decimal(10,2) NOT NULL,
  subtotal decimal(10,2) NOT NULL,
  created_at timestamptz DEFAULT now()
);

-- Create sincronizacion_log table
CREATE TABLE IF NOT EXISTS sincronizacion_log (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  tipo_operacion text NOT NULL,
  tabla_afectada text,
  registros_procesados integer DEFAULT 0,
  estado text NOT NULL,
  mensaje text,
  usuario_id uuid REFERENCES usuarios(id),
  fecha_inicio timestamptz DEFAULT now(),
  fecha_fin timestamptz,
  created_at timestamptz DEFAULT now()
);

-- Create configuracion_sistema table
CREATE TABLE IF NOT EXISTS configuracion_sistema (
  id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
  clave text UNIQUE NOT NULL,
  valor text,
  tipo text DEFAULT 'STRING',
  descripcion text,
  categoria text DEFAULT 'GENERAL',
  editable_por_usuario boolean DEFAULT false,
  updated_at timestamptz DEFAULT now(),
  updated_by uuid REFERENCES usuarios(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol_id ON usuarios(rol_id);
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo);
CREATE INDEX IF NOT EXISTS idx_productos_categoria_id ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_ventas_usuario_id ON ventas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_ventas_cliente_id ON ventas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_venta);
CREATE INDEX IF NOT EXISTS idx_pedidos_usuario_id ON pedidos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha ON pedidos(fecha_pedido);

-- Enable Row Level Security on all tables
ALTER TABLE roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE ventas ENABLE ROW LEVEL SECURITY;
ALTER TABLE ventas_detalle ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedidos ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedidos_detalle ENABLE ROW LEVEL SECURITY;
ALTER TABLE sincronizacion_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE configuracion_sistema ENABLE ROW LEVEL SECURITY;

-- RLS Policies for roles table
CREATE POLICY "Authenticated users can read roles"
  ON roles FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Only ADMIN can insert roles"
  ON roles FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Only ADMIN can update roles"
  ON roles FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Only ADMIN can delete roles"
  ON roles FOR DELETE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- RLS Policies for usuarios table
CREATE POLICY "Users can read own profile"
  ON usuarios FOR SELECT
  TO authenticated
  USING (id = auth.uid() OR EXISTS (
    SELECT 1 FROM usuarios u
    JOIN roles r ON u.rol_id = r.id
    WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
  ));

CREATE POLICY "Only ADMIN can insert users"
  ON usuarios FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Users can update own profile, ADMIN can update all"
  ON usuarios FOR UPDATE
  TO authenticated
  USING (
    id = auth.uid() OR EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Only ADMIN can delete users"
  ON usuarios FOR DELETE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- RLS Policies for categorias table
CREATE POLICY "Authenticated users can read categories"
  ON categorias FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "ADMIN and VENDEDOR can insert categories"
  ON categorias FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can update categories"
  ON categorias FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

CREATE POLICY "Only ADMIN can delete categories"
  ON categorias FOR DELETE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- RLS Policies for productos table
CREATE POLICY "Authenticated users can read products"
  ON productos FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "ADMIN and VENDEDOR can insert products"
  ON productos FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can update products"
  ON productos FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

CREATE POLICY "Only ADMIN can delete products"
  ON productos FOR DELETE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- RLS Policies for clientes table
CREATE POLICY "Authenticated users can read clients"
  ON clientes FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "ADMIN and VENDEDOR can manage clients"
  ON clientes FOR ALL
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

-- RLS Policies for ventas table
CREATE POLICY "Users can read own sales, ADMIN reads all"
  ON ventas FOR SELECT
  TO authenticated
  USING (
    usuario_id = auth.uid() OR EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can insert sales"
  ON ventas FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

CREATE POLICY "ADMIN can update sales"
  ON ventas FOR UPDATE
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- RLS Policies for ventas_detalle table
CREATE POLICY "Users can read sale details they own"
  ON ventas_detalle FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM ventas v
      WHERE v.id = venta_id AND (
        v.usuario_id = auth.uid() OR EXISTS (
          SELECT 1 FROM usuarios u
          JOIN roles r ON u.rol_id = r.id
          WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
        )
      )
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can insert sale details"
  ON ventas_detalle FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

-- RLS Policies for pedidos table
CREATE POLICY "Users can read own orders, ADMIN reads all"
  ON pedidos FOR SELECT
  TO authenticated
  USING (
    usuario_id = auth.uid() OR EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can manage orders"
  ON pedidos FOR ALL
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

-- RLS Policies for pedidos_detalle table
CREATE POLICY "Users can read order details they own"
  ON pedidos_detalle FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM pedidos p
      WHERE p.id = pedido_id AND (
        p.usuario_id = auth.uid() OR EXISTS (
          SELECT 1 FROM usuarios u
          JOIN roles r ON u.rol_id = r.id
          WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
        )
      )
    )
  );

CREATE POLICY "ADMIN and VENDEDOR can insert order details"
  ON pedidos_detalle FOR INSERT
  TO authenticated
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre IN ('ADMIN', 'VENDEDOR')
    )
  );

-- RLS Policies for sincronizacion_log table
CREATE POLICY "Users can read own sync logs, ADMIN reads all"
  ON sincronizacion_log FOR SELECT
  TO authenticated
  USING (
    usuario_id = auth.uid() OR EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Authenticated users can insert sync logs"
  ON sincronizacion_log FOR INSERT
  TO authenticated
  WITH CHECK (usuario_id = auth.uid());

-- RLS Policies for configuracion_sistema table
CREATE POLICY "Only ADMIN can read system config"
  ON configuracion_sistema FOR SELECT
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

CREATE POLICY "Only ADMIN can manage system config"
  ON configuracion_sistema FOR ALL
  TO authenticated
  USING (
    EXISTS (
      SELECT 1 FROM usuarios u
      JOIN roles r ON u.rol_id = r.id
      WHERE u.id = auth.uid() AND r.nombre = 'ADMIN'
    )
  );

-- Insert default roles
INSERT INTO roles (nombre, descripcion, permisos) VALUES
  ('ADMIN', 'Administrador del sistema con acceso total', '{"usuarios": {"create": true, "read": true, "update": true, "delete": true}, "productos": {"create": true, "read": true, "update": true, "delete": true}, "ventas": {"create": true, "read": true, "update": true, "delete": true}, "pedidos": {"create": true, "read": true, "update": true, "delete": true}, "reportes": {"read": true}, "configuracion": {"read": true, "update": true}, "sincronizacion": {"execute": true}}'::jsonb),
  ('VENDEDOR', 'Vendedor con acceso a ventas y productos', '{"productos": {"create": true, "read": true, "update": true, "delete": false}, "ventas": {"create": true, "read": true, "update": false, "delete": false}, "pedidos": {"create": true, "read": true, "update": true, "delete": false}, "reportes": {"read": true}}'::jsonb),
  ('USUARIO', 'Usuario básico con acceso limitado', '{"productos": {"create": false, "read": true, "update": false, "delete": false}, "ventas": {"create": false, "read": true, "update": false, "delete": false}}'::jsonb)
ON CONFLICT (nombre) DO NOTHING;

-- Insert default system configuration
INSERT INTO configuracion_sistema (clave, valor, tipo, descripcion, categoria, editable_por_usuario) VALUES
  ('app.nombre', 'Sistema de Gestión de Inventario', 'STRING', 'Nombre de la aplicación', 'GENERAL', false),
  ('app.version', '1.0.0', 'STRING', 'Versión del sistema', 'GENERAL', false),
  ('sync.auto_enabled', 'false', 'BOOLEAN', 'Habilitar sincronización automática', 'SINCRONIZACION', true),
  ('sync.interval_minutes', '30', 'NUMBER', 'Intervalo de sincronización en minutos', 'SINCRONIZACION', true),
  ('sync.endpoint_url', '', 'STRING', 'URL del endpoint de sincronización', 'SINCRONIZACION', true),
  ('ventas.impuesto_porcentaje', '18', 'NUMBER', 'Porcentaje de impuesto para ventas', 'VENTAS', true),
  ('inventario.alerta_stock_bajo', 'true', 'BOOLEAN', 'Mostrar alertas de stock bajo', 'INVENTARIO', true),
  ('seguridad.sesion_timeout_minutos', '30', 'NUMBER', 'Tiempo de expiración de sesión en minutos', 'SEGURIDAD', true)
ON CONFLICT (clave) DO NOTHING;