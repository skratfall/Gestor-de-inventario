# 🎨 Guía Visual de Diseño - Ejemplos y Especificaciones

## 📐 Sistema de Diseño

### Estructura Visual General

```
┌─────────────────────────────────────────────────────┐
│                  TOP HEADER                         │
│    Título │                          │ Info Usuario │
├─────────┬───────────────────────────────────────────┤
│         │                                           │
│ SIDEBAR │         MAIN CONTENT AREA                 │
│         │                                           │
│         │  • Tarjetas de Dashboard                  │
│         │  • Tablas de Datos                        │
│         │  • Formularios                            │
│         │                                           │
└─────────┴───────────────────────────────────────────┘
```

---

## 🎯 Componentes Básicos

### 1. BOTONES

#### Botón Primario (Guardar)
```
┌──────────────┐
│   Guardar    │  ← Azul (#3498db)
└──────────────┘
  Sombra suave
  Hover: Azul más oscuro + elevado
```

**Especificaciones:**
- Padding: 10px vertical, 18px horizontal
- Border-radius: 6px
- Font-weight: 500
- Font-size: 13px
- Efecto hover: -2px translate Y + sombra aumentada

#### Botón Peligro (Eliminar)
```
┌──────────────┐
│   Eliminar   │  ← Rojo (#e74c3c)
└──────────────┘
```

#### Botón Éxito (Confirmar)
```
┌──────────────┐
│   Confirmar  │  ← Verde (#27ae60)
└──────────────┘
```

---

### 2. CAMPOS DE ENTRADA

#### Campo de Texto Normal
```
┌────────────────────────────┐
│ Ingresa tu nombre...       │
└────────────────────────────┘
Borde: 1px gris (#ecf0f1)
Radio: 6px
En foco: Borde azul 2px + sombra azul
```

#### Campo de Búsqueda
```
┌────────────────────────────────────────────┐
│ 🔍 Buscar usuarios...                      │
└────────────────────────────────────────────┘
Más compacto: 8px padding
```

#### Campo de Login
```
┌────────────────────────────┐
│ usuario@example.com        │  ← Fondo gris (#f8f9fa)
└────────────────────────────┘
En foco: Fondo blanco + borde azul
```

---

### 3. TABLAS

#### Estructura de Tabla

```
┏━━━━━━━━━━━━┳━━━━━━━━━━━━┳━━━━━━━━━━━━┓
┃ Nombre     ┃ Email      ┃ Estado     ┃  ← Gradiente azul
┣━━━━━━━━━━━━╋━━━━━━━━━━━━╋━━━━━━━━━━━━┫
┃ Juan Pérez ┃ juan@...   ┃ Activo     ┃  ← Fondo blanco
┣━━━━━━━━━━━━╋━━━━━━━━━━━━╋━━━━━━━━━━━━┫
┃ Ana Gómez  ┃ ana@...    ┃ Activo     ┃  ← Fondo gris claro
┣━━━━━━━━━━━━╋━━━━━━━━━━━━╋━━━━━━━━━━━━┫
┃ Luis López ┃ luis@...   ┃ Inactivo   ┃  ← Al hover: más gris
┗━━━━━━━━━━━━┻━━━━━━━━━━━━┻━━━━━━━━━━━━┛

Características:
- Encabezado: Gradiente #1e3a5f → #34495e
- Texto encabezado: Blanco, bold
- Filas alternadas: Blanco y #f8f9fa
- Hover: #ecf0f1
- Selección: #d6eaf8 (azul translúcido)
- Radio: 8px
- Borde: 1px #ecf0f1
```

---

### 4. TARJETAS (CARDS)

#### Card Estándar
```
┌────────────────────────────┐
│ Título de la Tarjeta       │
│                            │
│ Contenido de la tarjeta    │
│                            │
│ Información adicional      │
└────────────────────────────┘
Borde: 1px gris (#ecf0f1)
Radio: 8px
Padding: 20px
Sombra: Suave
Hover: Sombra aumentada
```

#### Dashboard Card - Primaria
```
│ ┌────────────────────────────┐
│ │ 👥 Usuarios Activos        │
│ │                            │
│ │ 42                         │
│ │                            │
│ │ Ver detalles →             │
│ └────────────────────────────┘
Borde izquierdo: 4px azul (#3498db)
```

#### Dashboard Card - Éxito
```
│ ┌────────────────────────────┐
│ │ ✓ Roles Configurados       │
│ │                            │
│ │ 5                          │
│ │                            │
│ │ Gestionar →                │
│ └────────────────────────────┘
Borde izquierdo: 4px verde (#27ae60)
```

---

### 5. SIDEBAR

```
┌──────────────────────┐
│   LOGO 180x180       │
│                      │  ← Fondo gradiente:
│ Panel Administrativo │     #1e3a5f → #34495e
│ Gestión Inventario   │
│ Administrador        │
├──────────────────────┤
│ ADMINISTRACIÓN       │  ← Label gris
│ 🏠 Dashboard      [A]│  ← [A] = Activo (azul)
│ 👥 Usuarios          │
│ 🔐 Roles             │
├──────────────────────┤
│ SISTEMA              │
│ 🔄 Sincronización    │
│ ⚙️  Configuración    │
│ 🛡️  Seguridad        │
├──────────────────────┤
│                      │
│       (espacio)      │
│                      │
├──────────────────────┤
│ 🚪 Cerrar Sesión  [L]│  ← [L] = Logout (rojo)
└──────────────────────┘

Ancho: 280px
Botón activo: Fondo azul + borde izq 4px
Botón hover: Fondo azul translúcido
```

---

### 6. ENCABEZADO DE PÁGINA

```
┌────────────────────────────────────────────────────────┐
│ Bienvenido al Sistema      │  Usuario: Admin           │
│                            │  Última sesión: Hoy       │
│                            │  Fecha: Lun, 29 Oct 2025  │
│                            │  Hora: 10:30:45 AM        │
└────────────────────────────────────────────────────────┘

Fondo: Blanco
Borde inferior: 1px gris
Sombra: Sutil
Padding: 15px 25px
```

---

## 🎨 PALETA DE COLORES EN ACCIÓN

### Escala de Azules
```
Azul Oscuro:    #1e3a5f  ■■■■■ (Encabezados principales)
Azul Primario:  #3498db  ■■■■■ (Botones, links)
Azul Oscuro 2:  #34495e  ■■■■■ (Secundarios)
Azul Selección: #d6eaf8  ■■■■■ (Background fila seleccionada)
```

### Semánticos
```
Éxito/Verde:    #27ae60  ■■■■■ (Confirmación, guardado)
Advertencia:    #f39c12  ■■■■■ (Atención, sincronización)
Peligro/Rojo:   #e74c3c  ■■■■■ (Eliminar, error)
```

### Escala Gris
```
Blanco:         #ffffff  ■■■■■ (Fondo tarjetas, campos)
Gris Claro:     #f8f9fa  ■■■■■ (Filas alternadas)
Gris Borde:     #ecf0f1  ■■■■■ (Bordes, separadores)
Gris Oscuro:    #7f8c8d  ■■■■■ (Texto secundario)
```

---

## 📏 ESPECIFICACIONES DE ESPACIADO

```
Padding estándar:     15-20px
Spacing entre comps:  10-15px
Margin horizontal:    25px (secciones principales)
Margin vertical:      30px (secciones principales)

Ejemplo:
┌─────────────────────────┐
│    Padding: 20px        │
│                         │
│  ┌──────────┐ spacing   │
│  │ Elemento │ 15px      │
│  └──────────┘           │
│                         │
│  ┌──────────┐           │
│  │ Elemento │           │
│  └──────────┘           │
│                         │
└─────────────────────────┘
```

---

## 🌈 EJEMPLOS DE VISTAS

### Vista de Login

```
    ┌─────────────────────────────────┐
    │        Fondo Gradiente          │
    │         Azul oscuro             │
    │                                 │
    │      ┌───────────────────┐      │
    │      │   Logo 160x160    │      │
    │      └───────────────────┘      │
    │                                 │
    │    Bienvenido de vuelta         │
    │    Ingresa tus credenciales     │
    │                                 │
    │    ┌─────────────────────┐      │
    │    │ 👤 Usuario          │      │
    │    │ [                 ] │      │
    │    └─────────────────────┘      │
    │                                 │
    │    ┌─────────────────────┐      │
    │    │ 🔒 Contraseña       │      │
    │    │ [                 ] │      │
    │    └─────────────────────┘      │
    │                                 │
    │    ┌─────────────────────┐      │
    │    │ Iniciar Sesión      │ ← Azul
    │    └─────────────────────┘      │
    │                                 │
    │    ┌─────────────────────┐      │
    │    │ Cancelar            │ ← Gris
    │    └─────────────────────┘      │
    │                                 │
    │    💡 Modo Demo                 │
    │    Usa cualquier usuario        │
    │    Ejemplo: admin / admin123    │
    │                                 │
    └─────────────────────────────────┘

Radio de Card: 12px (más redondeado)
Padding: 40px
Sombra: Fuerte
```

### Dashboard Principal

```
┌─────────────────────────────────────────────────┐
│ 🏠 Dashboard         │ Admin │ Lun 29 Oct 2025 │
├──────────┬───────────────────────────────────────┤
│ 🏠 Dashboard   ● ╱╱   │ Estado del Sistema         │
│ 👥 Usuarios   ╱  │   │                            │
│ 🔐 Roles    │        │ ┌──────────┬──────────┐   │
│             │        │ │ 👥 Usuarios │ 🔐 Roles    │
│ 🔄 Sync  │  ─       │ │ 42         │ 5           │
│ ⚙️  Config       │        │ └──────────┴──────────┘   │
│ 🛡️  Security      │        │                            │
│             │        │ ⚡ Acciones Rápidas        │
│ 🚪 Logout        │        │ ┌──┬──┬──┬──┐            │
│             │        │ │👤│🔐│🔄│🛡│            │
└─────────────────────┴───────────────────────────┘
```

---

## ✨ EFECTOS Y TRANSICIONES

### Efecto Hover en Botones

**Estado Normal:**
```
┌──────────────┐
│   Guardar    │  (Y: 0)
└──────────────┘
Sombra: 3px blur
```

**Estado Hover:**
```
   ┌──────────────┐
   │   Guardar    │  (Y: -2px, elevado)
   └──────────────┘
Sombra: 6px blur (aumentada)
```

### Efecto Focus en Campos

**Estado Normal:**
```
┌────────────────┐
│ Usuario...     │
└────────────────┘
Borde: 1px gris
```

**Estado Focus:**
```
┌────────────────┐
│ usuario@ex.com │ ← Texto visible
└────────────────┘
Borde: 2px azul
Sombra: Azul translúcido
Fondo: Blanco
```

---

## 📊 GRID Y LAYOUT

### Dashboard Cards Grid (3 columnas)

```
┌────────────────┬────────────────┬────────────────┐
│ Card Primary   │ Card Success   │ Card Info      │
│ (Azul)         │ (Verde)        │ (Azul claro)   │
├────────────────┼────────────────┼────────────────┤
│ Card Warning   │ Card Secondary │ Card Config    │
│ (Naranja)      │ (Gris)         │ (Púrpura)      │
└────────────────┴────────────────┴────────────────┘

Gap horizontal: 25px
Gap vertical: 25px
Altura mínima card: 150px
Cada columna: 33.33% ancho
```

---

## 🎯 JERARQUÍA VISUAL

### Pesos de Fuente

```
Normal (400):      Lorem ipsum dolor sit amet
Medio (500):       Lorem ipsum dolor sit amet
Negrita (bold):    Lorem ipsum dolor sit amet
```

### Tamaños de Fuente

```
28px  → Títulos de login
24px  → Títulos de módulos principales
22px  → Títulos de página
16px  → Títulos de secciones
14px  → Texto normal
13px  → Etiquetas, pequeños
12px  → Información secundaria
11px  → Notas, información mínima
```

---

## ♿ CONSIDERACIONES DE ACCESIBILIDAD

### Contraste de Color

```
ACEPTABLE (4.5:1 mínimo):
- Blanco (#fff) sobre Azul oscuro (#1e3a5f) ✓
- Negro (#2c3e50) sobre Blanco (#fff) ✓
- Blanco sobre Rojo (#e74c3c) ✓

VERIFICAR:
- Gris (#7f8c8d) sobre Gris claro (#f8f9fa) ≈ 2:1 ⚠️
```

### Focus Visible

```
Todos los componentes interactivos tienen:
- Border ancho visible (2px cuando en foco)
- Color de borde contrastante
- Sombra de foco visible
```

---

**Versión:** 1.0  
**Última actualización:** 9 de diciembre de 2025  
**Estado:** ✅ Completo
