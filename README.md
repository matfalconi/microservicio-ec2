# Microservicio de ejemplo

Microservicio Spring Boot para despliegue en AWS EC2 mediante GitHub Actions y Docker Hub.

## Estructura
- Java 17 + Spring Boot 3.2.5
- Maven
- Dockerfile multi-stage
- GitHub Actions workflow

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/microservicio/{id}` | Obtener recurso por id |
| POST | `/microservicio` | Crear recurso (body JSON con `mensaje`) |
| PUT | `/microservicio?status=OK` | Actualizar status |
| DELETE | `/microservicio` | Eliminar (header `Authorization`) |

## Correr localmente

```bash
mvn clean package
java -jar target/microservicio.jar
```

## Correr con Docker

```bash
docker build -t microservicio:1.0 .
docker run -d -p 8080:8080 --name microservicio-ejemplo microservicio:1.0
```

## Secretos requeridos en GitHub
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SESSION_TOKEN`
- `EC2_HOST`
- `EC2_SSH_KEY`
- `USER_SERVER`
