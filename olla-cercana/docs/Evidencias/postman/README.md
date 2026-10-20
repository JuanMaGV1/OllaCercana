# Set de Pruebas de Funcionamiento (Postman) — OllaCercana

Este documento detalla las pruebas de integración automatizadas para el backend de OllaCercana, las cuales validan el flujo completo de negocio y las reglas de seguridad.

## Archivos de la Colección
- **Colección Principal:** `OllaCercana_Coleccion.postman_collection.json`
- **Entorno Local:** `Local_QA.postman_environment.json`
- **Entorno Azure (PROD):** `Azure_PROD.postman_environment.json`

## ¿Cómo ejecutar las pruebas?
1. Importa la colección y el entorno deseado en Postman.
2. Selecciona el entorno **Local - QA** (o Azure - PROD) en la esquina superior derecha.
3. Abre el **Collection Runner** y ejecuta la colección completa.

## Tabla de Casos de Prueba (Evidencias)

| ID | Funcionalidad | Escenario | Request | Esperado | Obtenido | Evidencia |
|:---|:---|:---|:---|:---|:---|:---|
| CP-01 | Auth | Login exitoso (Obtiene JWT) | `POST /sesiones` | 200 |  200 | [Ver Captura](/olla-cercana/docs/Evidencias/postman/Login-JWT.png) |
| CP-02 | Platos | Crear plato (Rol Cocinera) | `POST /platos` | 201 | 201 | [Ver Captura](/olla-cercana/docs/Evidencias/postman/Plato-Creado.png) |
| CP-03 | Reservas | Crear reserva (Happy Path) | `POST /reservas` | 201 | 201 | [Ver Captura](/olla-cercana/docs/Evidencias/postman/Reserva-Creada.png) |
| CP-04 | Seguridad | Petición sin Token JWT | `GET /notificaciones` | 401 | 401 | [Ver Captura](/olla-cercana/docs/Evidencias/postman/Chat%20-%20Sin%20Token.png) |
| CP-05 | Seguridad | Rol incorrecto (Comprador crea plato) | `POST /platos` | 403 | 403 | [Ver Captura](/olla-cercana/docs/Evidencias/postman/Plato%20-%20Comprador%20Crea%20Plato.png) |