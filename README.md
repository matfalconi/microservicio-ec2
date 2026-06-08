# Microservicio - Gestion de Guias de Despacho

Solucion Cloud Native con Spring Boot, integrada con AWS (EFS + S3) y desplegada
automaticamente con GitHub Actions sobre una instancia EC2.

## Tecnologias

- Java 17 + Spring Boot 3.2.5
- AWS SDK para S3
- iText (generacion de PDF)
- Docker + GitHub Actions (CI/CD)
- AWS EFS, S3 y EC2

## Endpoints (base: /api/guias)

| Metodo | Ruta | Funcion | Criterio |
|--------|------|---------|----------|
| POST   | `/api/guias` | Crea la guia y la guarda en EFS | 1 |
| POST   | `/api/guias/{id}/subir` | Sube la guia a S3 | 2 |
| PUT    | `/api/guias/{id}` | Modifica/actualiza la guia en S3 | 3 |
| GET    | `/api/guias/{id}/descargar?periodo=&transportista=` | Descarga (con permisos) | 4 |
| GET    | `/api/guias?periodo=&transportista=` | Consulta el historial | 5 |
| DELETE | `/api/guias/{id}?periodo=&transportista=` | Elimina una guia | - |

Estructura en S3: `{periodo}/{transportista}/{idGuia}.pdf` (ej: `20211/transportistaX/guia123.pdf`)

## Ejemplo: crear una guia

```bash
curl -X POST http://TU-IP:8080/api/guias \
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

## Despliegue (CI/CD)

Cada push a `main` ejecuta `.github/workflows/deploy.yml`, que construye la imagen
Docker, la publica en Docker Hub y la despliega en EC2.

### Secretos en GitHub (Settings > Secrets and variables > Actions)

- `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
- `EC2_HOST`, `USER_SERVER`, `EC2_SSH_KEY`
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SESSION_TOKEN`

## Configuracion AWS

- EFS montado en la EC2 en `/mnt/efs/guias`
- Bucket S3 (ajustar nombre en `application.properties` y en `deploy.yml`)
- Credenciales pasadas al contenedor por variables de entorno
