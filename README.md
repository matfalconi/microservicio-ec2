# Sistema de Gestión de Pedidos y Generación de Guías de Despacho
 
Solución **Cloud Native** desarrollada para una empresa transportista, que permite generar
guías de despacho en PDF, almacenarlas temporalmente en **AWS EFS** y subirlas de forma
organizada a **AWS S3**, con despliegue automatizado mediante **CI/CD (GitHub Actions →
Docker Hub → EC2)**.
 
> Asignatura: Desarrollo Cloud Native (CDY2204) — Semana 3 — Actividad Sumativa Grupal.
 
---
 
## Tabla de contenidos
 
1. [Descripción del caso](#1-descripción-del-caso)
2. [Arquitectura](#2-arquitectura)
3. [Tecnologías](#3-tecnologías)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Endpoints REST](#5-endpoints-rest)
6. [Flujo de funcionamiento](#6-flujo-de-funcionamiento)
7. [Configuración AWS](#7-configuración-aws)
8. [Despliegue automatizado (CI/CD)](#8-despliegue-automatizado-cicd)
9. [Cómo ejecutar y probar](#9-cómo-ejecutar-y-probar)
---
 
## 1. Descripción del caso
 
Una empresa transportista necesita un sistema para **gestionar sus despachos** y **generar
las guías** correspondientes. Cada guía representa un pedido despachado (origen, destino,
carga, transportista) y se materializa en un documento PDF que debe quedar almacenado y
organizado en la nube para su posterior consulta, descarga o modificación.
 
El sistema resuelve este caso mediante un microservicio REST construido con Spring Boot,
integrado a servicios de almacenamiento de AWS y desplegado de forma continua.
 
---
 
## 2. Arquitectura
 
La aplicación se ejecuta como un contenedor Docker (Spring Boot) sobre una instancia
**EC2**. El cliente (Postman) realiza peticiones REST al microservicio, que opera sobre dos
servicios de almacenamiento:
 
- **AWS EFS**, montado en la instancia en `/mnt/efs/guias`, donde se guardan temporalmente
  las guías PDF recién generadas.
- **AWS S3**, donde las guías se almacenan de forma definitiva, organizadas en carpetas por
  período y transportista.
El flujo de datos es: el cliente solicita la creación de una guía → el microservicio genera
el PDF y lo guarda en el EFS → posteriormente la guía se sube a S3 con su estructura de
carpetas → las operaciones de descarga, modificación, consulta y eliminación se realizan
sobre los archivos en S3.
 
El **despliegue** es continuo: cada `push` a la rama `main` desencadena el pipeline de
GitHub Actions, que construye la imagen Docker, la publica en Docker Hub y la despliega en
la instancia EC2.
 
---
 
## 3. Tecnologías
 
| Componente | Tecnología |
|------------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Generación de PDF | iText 7 |
| Almacenamiento temporal | AWS EFS |
| Almacenamiento definitivo | AWS S3 (AWS SDK v2) |
| Contenedor | Docker |
| CI/CD | GitHub Actions |
| Registro de imágenes | Docker Hub |
| Cómputo | AWS EC2 |
 
---
 
## 4. Estructura del proyecto
 
El código sigue una arquitectura por capas, con responsabilidades separadas:
 
```
src/main/java/com/ejemplo/microservicio/
├── MicroservicioApplication.java     # Punto de entrada
├── config/
│   └── AwsConfig.java                # Configuración del cliente S3
├── controller/
│   └── GuiaController.java           # Endpoints REST
├── service/
│   ├── EfsStorageService.java        # Genera PDF y guarda en EFS
│   └── S3StorageService.java         # Operaciones con S3
├── model/
│   └── Guia.java                     # Modelo de datos
└── exception/
    └── ManejadorErrores.java         # Manejo centralizado de errores
```
 
---
 
## 5. Endpoints REST
 
Ruta base: `/api/guias`
 
| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/guias` | Crea la guía, genera el PDF y lo guarda en EFS |
| `POST` | `/api/guias/{id}/subir` | Sube la guía generada a S3 |
| `GET` | `/api/guias/{id}/descargar?periodo={p}&transportista={t}` | Descarga la guía (con validación de permisos) |
| `PUT` | `/api/guias/{id}` | Modifica/actualiza la guía en S3 |
| `DELETE` | `/api/guias/{id}?periodo={p}&transportista={t}` | Elimina una guía específica |
| `GET` | `/api/guias?periodo={p}&transportista={t}` | Consulta el historial por transportista y período |
 
**Organización en S3:** `{periodo}/{transportista}/{idGuia}.pdf`
Ejemplo: `20211/transportistaX/guia123.pdf` (coincide con el ejemplo del enunciado).
 
**Validación de permisos (descarga):** se realiza mediante el encabezado `X-Usuario-Rol`.
Solo los roles `ADMIN` o `TRANSPORTISTA` pueden descargar; en caso contrario se responde
`403 Forbidden`.
 
---
 
## 6. Flujo de funcionamiento
 
1. **Crear** (`POST /api/guias`): se genera el PDF de la guía y se guarda en
   `/mnt/efs/guias/{periodo}/{transportista}/{id}.pdf` (EFS).
2. **Subir** (`POST /api/guias/{id}/subir`): el PDF se transfiere desde el EFS a S3,
   respetando la estructura de carpetas.
3. **Consultar** (`GET /api/guias`): lista las guías de un transportista en un período.
4. **Descargar** (`GET .../descargar`): obtiene el PDF desde S3, validando el rol del usuario.
5. **Modificar** (`PUT /api/guias/{id}`): regenera el PDF con los nuevos datos y reemplaza
   el archivo en S3.
6. **Eliminar** (`DELETE /api/guias/{id}`): borra la guía de S3.
---
 
## 7. Configuración AWS
 
| Recurso | Configuración |
|---------|--------------|
| **EFS** | Sistema de archivos montado en la EC2 en `/mnt/efs/guias`. Regla NFS (puerto 2049) habilitada en el grupo de seguridad. |
| **S3** | Bucket en la región `us-east-1`. Las guías se almacenan organizadas por período y transportista. |
| **EC2** | Instancia con Docker. Las credenciales de AWS se entregan al contenedor por variables de entorno. |
 
### Variables de entorno
 
| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `AWS_REGION` | Región de AWS | `us-east-1` |
| `S3_BUCKET` | Nombre del bucket | `guiabucket-s3-2026` |
| `EFS_PATH` | Ruta de montaje del EFS | `/mnt/efs/guias` |
| `AWS_ACCESS_KEY_ID` | Credencial de acceso | — |
| `AWS_SECRET_ACCESS_KEY` | Credencial secreta | — |
| `AWS_SESSION_TOKEN` | Token de sesión (cuenta de laboratorio) | — |
 
---
 
## 8. Despliegue automatizado (CI/CD)
 
El archivo `.github/workflows/deploy.yml` automatiza el despliegue ante cada `push` a la
rama `main`:
 
1. **Checkout** del código del repositorio.
2. **Login** en Docker Hub con credenciales almacenadas como *secrets*.
3. **Build & Push**: construye la imagen Docker y la publica en Docker Hub.
4. **Deploy**: se conecta por SSH a la instancia EC2, descarga la imagen y levanta el
   contenedor montando el EFS y pasando las credenciales de AWS.
### Secrets requeridos en GitHub
 
`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `EC2_HOST`, `USER_SERVER`, `EC2_SSH_KEY`,
`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`.
 
---
 
## 9. Cómo ejecutar y probar
 
### Ejecutar localmente
 
```bash
mvn spring-boot:run
```
 
### Ejemplo: crear una guía
 
```bash
curl -X POST http://localhost:8080/api/guias \
  -H "Content-Type: application/json" \
  -d '{
    "id": "guia123",
    "transportista": "transportistaX",
    "periodo": "20211",
    "origen": "Santiago",
    "destino": "Valparaiso",
    "descripcionCarga": "Electrodomesticos",
    "fechaEmision": "2025-06-01"
  }'
```
 
### Ejemplo: subir a S3
 
```bash
curl -X POST http://localhost:8080/api/guias/guia123/subir \
  -H "Content-Type: application/json" \
  -d '{ "transportista": "transportistaX", "periodo": "20211" }'
```
 
### Ejemplo: descargar con permisos
 
```bash
curl "http://localhost:8080/api/guias/guia123/descargar?periodo=20211&transportista=transportistaX" \
  -H "X-Usuario-Rol: ADMIN" -o guia123.pdf
```
 
---

 
## Autores
 
Proyecto grupal — Desarrollo Cloud Native (CDY2204), Duoc UC.
