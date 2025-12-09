# 📚 Documentación Completa de la API REST

> **Versión:** 2.1.0  
> **Última actualización:** Noviembre 2025  
> **Autor:** Equipo de Desarrollo TelDev

---

## 🎯 Descripción General

Esta documentación describe los **endpoints** del servicio de gestión de productos, desarrollado con **Spring Boot 3.3**, que proporciona operaciones **CRUD completas** para el manejo del catálogo.

### Características Principales

- ✅ API RESTful completamente funcional
- ✅ Autenticación mediante JWT
- ✅ Respuestas en formato JSON
- ✅ Códigos de estado HTTP estándar
- ✅ Validación automática de datos
- ✅ Documentación Swagger/OpenAPI

---

## 🔧 Configuración Inicial

### Requisitos del Sistema

| Componente | Versión Mínima | Recomendada |
|------------|----------------|-------------|
| Java | 17 | 21 |
| Spring Boot | 3.2.0 | 3.3.5 |
| MySQL | 8.0 | 8.2 |
| Maven | 3.8.0 | 3.9.5 |

### Variables de Entorno

```bash
# Configuración de Base de Datos
DB_HOST=localhost
DB_PORT=3306
DB_NAME=products_db
DB_USER=admin
DB_PASSWORD=secure_password

# Configuración del Servidor
SERVER_PORT=8080
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION=86400000
```

### Instalación Rápida

```bash
# Clonar el repositorio
git clone https://github.com/empresa/api-productos.git
cd api-productos

# Instalar dependencias
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run
```

---

## 📡 Base URL

```
http://localhost:8080/api/v1
```

**Producción:**
```
https://api.teldev.com/v1
```

---

## 🔐 Autenticación

Todos los endpoints requieren un token JWT en el header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Obtener Token

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "admin@example.com",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "admin@example.com",
    "role": "ADMIN"
  }
}
```

---

## 📋 Endpoints

### 1. Listar Todos los Productos

Obtiene un listado completo de productos con paginación opcional.

**Endpoint:** `GET /products`

**Parámetros Query:**

| Parámetro | Tipo | Requerido | Descripción | Default |
|-----------|------|-----------|-------------|---------|
| page | integer | No | Número de página | 0 |
| size | integer | No | Elementos por página | 20 |
| sort | string | No | Campo de ordenamiento | name |
| order | string | No | Dirección (asc/desc) | asc |
| category | integer | No | Filtrar por categoría | - |
| minPrice | decimal | No | Precio mínimo | - |
| maxPrice | decimal | No | Precio máximo | - |

**Ejemplo de Request:**

```http
GET /api/v1/products?page=0&size=10&category=1&sort=price&order=desc
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Laptop Dell Inspiron 15",
      "description": "Laptop profesional con procesador Intel i7",
      "price": 3500.00,
      "stock": 10,
      "sku": "LAP-DELL-001",
      "category": {
        "id": 1,
        "name": "Computadoras",
        "slug": "computadoras"
      },
      "images": [
        "https://storage.example.com/products/laptop-dell-01.jpg"
      ],
      "createdAt": "2025-01-15T10:30:00Z",
      "updatedAt": "2025-11-12T08:45:00Z"
    },
    {
      "id": 2,
      "name": "Mouse Inalámbrico Logitech MX Master 3",
      "description": "Mouse ergonómico para productividad",
      "price": 85.50,
      "stock": 25,
      "sku": "MOU-LOG-002",
      "category": {
        "id": 2,
        "name": "Accesorios",
        "slug": "accesorios"
      },
      "images": [
        "https://storage.example.com/products/mouse-logitech-01.jpg"
      ],
      "createdAt": "2025-02-20T14:15:00Z",
      "updatedAt": "2025-11-10T16:20:00Z"
    }
  ],
  "pagination": {
    "currentPage": 0,
    "pageSize": 10,
    "totalPages": 5,
    "totalElements": 47
  }
}
```

**Códigos de Estado:**

| Código | Descripción |
|--------|-------------|
| 200 OK | Lista obtenida correctamente |
| 401 Unauthorized | Token inválido o expirado |
| 403 Forbidden | Sin permisos para acceder |
| 500 Internal Server Error | Error interno del servidor |

---

### 2. Obtener Producto por ID

Recupera la información detallada de un producto específico.

**Endpoint:** `GET /products/{id}`

**Parámetros de Ruta:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | integer | Identificador único del producto |

**Ejemplo de Request:**

```http
GET /api/v1/products/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Laptop Dell Inspiron 15",
    "description": "Laptop profesional con procesador Intel i7",
    "price": 3500.00,
    "stock": 10,
    "sku": "LAP-DELL-001",
    "category": {
      "id": 1,
      "name": "Computadoras",
      "slug": "computadoras"
    },
    "specifications": {
      "processor": "Intel Core i7-12700H",
      "ram": "16GB DDR4",
      "storage": "512GB NVMe SSD",
      "display": "15.6\" FHD IPS",
      "gpu": "Intel Iris Xe Graphics"
    },
    "images": [
      "https://storage.example.com/products/laptop-dell-01.jpg",
      "https://storage.example.com/products/laptop-dell-02.jpg"
    ],
    "tags": ["laptop", "dell", "profesional", "i7"],
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-11-12T08:45:00Z"
  }
}
```

**Response (404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "No se encontró el producto con ID: 999",
    "timestamp": "2025-11-12T10:30:00Z"
  }
}
```

---

### 3. Crear Nuevo Producto

Registra un nuevo producto en el sistema.

**Endpoint:** `POST /products`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer {token}
```

**Request Body:**

```json
{
  "name": "Teclado Mecánico Redragon K617",
  "description": "Teclado mecánico RGB 60%",
  "price": 190.00,
  "stock": 15,
  "sku": "TEC-RED-003",
  "categoryId": 2,
  "specifications": {
    "switches": "Red switches",
    "connectivity": "USB-C",
    "backlight": "RGB"
  },
  "tags": ["teclado", "mecanico", "gaming", "rgb"]
}
```

**Validaciones:**

| Campo | Reglas |
|-------|--------|
| name | Requerido, 3-200 caracteres |
| price | Requerido, mayor a 0 |
| stock | Requerido, entero no negativo |
| sku | Requerido, único, formato alfanumérico |
| categoryId | Requerido, categoría existente |

**Response (201 Created):**

```json
{
  "success": true,
  "message": "Producto creado exitosamente",
  "data": {
    "id": 48,
    "name": "Teclado Mecánico Redragon K617",
    "description": "Teclado mecánico RGB 60%",
    "price": 190.00,
    "stock": 15,
    "sku": "TEC-RED-003",
    "category": {
      "id": 2,
      "name": "Accesorios"
    },
    "createdAt": "2025-11-12T10:30:00Z"
  }
}
```

**Response (400 Bad Request):**

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Errores de validación",
    "details": [
      {
        "field": "price",
        "message": "El precio debe ser mayor a 0"
      },
      {
        "field": "sku",
        "message": "El SKU ya existe en el sistema"
      }
    ]
  }
}
```

---

### 4. Actualizar Producto

Modifica la información de un producto existente.

**Endpoint:** `PUT /products/{id}`

**Parámetros de Ruta:**

| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | integer | ID del producto a actualizar |

**Request Body:**

```json
{
  "name": "Laptop Dell Inspiron 15 (Actualizado)",
  "price": 3700.00,
  "stock": 8
}
```

> **Nota:** Solo se actualizan los campos enviados en el request (PATCH parcial).

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Producto actualizado correctamente",
  "data": {
    "id": 1,
    "name": "Laptop Dell Inspiron 15 (Actualizado)",
    "price": 3700.00,
    "stock": 8,
    "updatedAt": "2025-11-12T11:00:00Z"
  }
}
```

---

### 5. Eliminar Producto

Elimina un producto del sistema (soft delete).

**Endpoint:** `DELETE /products/{id}`

**Response (200 OK):**

```json
{
  "success": true,
  "message": "Producto eliminado exitosamente",
  "deletedAt": "2025-11-12T11:15:00Z"
}
```

**Response (404 Not Found):**

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "No se encontró el producto con ID: 999"
  }
}
```

---

## 🔄 Casos de Uso Comunes

### Búsqueda de Productos

```javascript
// Ejemplo con JavaScript Fetch API
async function searchProducts(query) {
  const response = await fetch(`/api/v1/products/search?q=${query}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
}
```

### Actualización de Stock

```java
// Ejemplo con Java Spring RestTemplate
public void updateStock(Long productId, Integer newStock) {
    String url = baseUrl + "/products/" + productId;
    
    Map<String, Object> request = new HashMap<>();
    request.put("stock", newStock);
    
    restTemplate.put(url, request);
}
```

### Filtrado Avanzado

```python
# Ejemplo con Python Requests
import requests

def get_filtered_products():
    params = {
        'category': 1,
        'minPrice': 100,
        'maxPrice': 5000,
        'sort': 'price',
        'order': 'asc'
    }
    
    response = requests.get(
        'http://localhost:8080/api/v1/products',
        params=params,
        headers={'Authorization': f'Bearer {token}'}
    )
    
    return response.json()
```

---

## ⚠️ Manejo de Errores

### Estructura de Respuesta de Error

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Descripción legible del error",
    "details": [],
    "timestamp": "2025-11-12T10:30:00Z",
    "path": "/api/v1/products/123"
  }
}
```

### Códigos de Error Comunes

| Código HTTP | Error Code | Descripción |
|-------------|------------|-------------|
| 400 | VALIDATION_ERROR | Datos de entrada inválidos |
| 401 | UNAUTHORIZED | Token inválido o expirado |
| 403 | FORBIDDEN | Sin permisos suficientes |
| 404 | NOT_FOUND | Recurso no encontrado |
| 409 | CONFLICT | Conflicto (ej: SKU duplicado) |
| 422 | UNPROCESSABLE_ENTITY | Entidad no procesable |
| 429 | RATE_LIMIT_EXCEEDED | Límite de peticiones excedido |
| 500 | INTERNAL_ERROR | Error interno del servidor |

---

## 📊 Rate Limiting

La API implementa límites de tasa para prevenir abuso:

| Tier | Límite | Ventana de Tiempo |
|------|--------|-------------------|
| Free | 100 requests | 1 hora |
| Basic | 1,000 requests | 1 hora |
| Premium | 10,000 requests | 1 hora |
| Enterprise | Ilimitado | - |

**Headers de Rate Limit:**

```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1699875600
```

---

## 🧪 Testing

### Ejemplo con cURL

```bash
# Listar productos
curl -X GET "http://localhost:8080/api/v1/products" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"

# Crear producto
curl -X POST "http://localhost:8080/api/v1/products" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Producto de Prueba",
    "price": 99.99,
    "stock": 50,
    "categoryId": 1
  }'
```

### Colección de Postman

Descarga la colección completa: [API_Products.postman_collection.json](./postman/collection.json)

---

## 🛠️ Tecnologías Utilizadas

### Backend

- **Framework:** Spring Boot 3.3.5
- **Lenguaje:** Java 21
- **ORM:** Spring Data JPA / Hibernate
- **Base de Datos:** MySQL 8.2
- **Seguridad:** Spring Security + JWT
- **Validación:** Jakarta Bean Validation
- **Documentación:** SpringDoc OpenAPI

### Herramientas de Desarrollo

- **Build Tool:** Maven 3.9.5
- **Testing:** JUnit 5, Mockito, TestContainers
- **Code Quality:** SonarQube, Checkstyle
- **CI/CD:** GitHub Actions
- **Containerization:** Docker, Docker Compose

---

## 📝 Changelog

### Version 2.1.0 (2025-11-12)

**✨ Features:**
- Agregado soporte para especificaciones de producto
- Implementado sistema de tags
- Añadida búsqueda por texto completo

**🐛 Bug Fixes:**
- Corregido error en paginación cuando no hay resultados
- Solucionado problema de validación de SKU duplicado

**🔒 Security:**
- Actualizado Spring Security a 6.2.0
- Implementado rate limiting por IP

### Version 2.0.0 (2025-10-01)

**🚀 Breaking Changes:**
- Migración a Spring Boot 3.x
- Cambio de estructura de respuestas JSON
- Nuevo sistema de autenticación JWT

---

## 📞 Soporte

**Email:** soporte@teldev.com  
**Slack:** [#api-support](https://teldev.slack.com/archives/api-support)  
**Docs:** [https://docs.teldev.com](https://docs.teldev.com)  
**Status Page:** [https://status.teldev.com](https://status.teldev.com)

---

## 📄 Licencia

Este proyecto está licenciado bajo MIT License - ver el archivo [LICENSE](./LICENSE) para más detalles.

---

## 👥 Contribuidores

| Nombre | Rol | GitHub |
|--------|-----|--------|
| Jesús León | Lead Developer | [@jesusleon](https://github.com/jesusleon) |
| María García | Backend Developer | [@mariagarcia](https://github.com/mariagarcia) |
| Carlos Pérez | DevOps Engineer | [@carlosperez](https://github.com/carlosperez) |

---

**© 2025 TelDev - Todos los derechos reservados**
