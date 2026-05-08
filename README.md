# Nombre del Proyecto

> Breve descripción de una línea sobre qué hace el sistema y qué problema resuelve.

---

# Tabla de Contenido

- Descripción General
- Equipo
- Objetivos
- Planteamiento del Problema
- Requerimientos
- Arquitectura
- Stack Tecnológico
- Diagramas
- Gestión del Proyecto
- Pruebas y Calidad
- Demo
- Instalación
- Referencias

---

#  Equipo

**Nombre del equipo**

| Integrante | Rol |
|-----------|------|
| Joshua David Quiroga Landazabal | Arquitecto Backend |
| Juan Manuel Lopez | Arquitecto DevOps |
| Laura Valentina Santiago Marquez | Lider |
| Juan Sebastian Murcia Yanquen | Frontend |

# Descripción General

## Resumen Ejecutivo

Descripción breve del proyecto:

- Problema a resolver.
- Solución propuesta.
- Usuarios objetivo.
- Impacto esperado.

## Alcance

### Incluye
- Funcionalidad 1
- Funcionalidad 2
- Funcionalidad 3

### No Incluye
- Funcionalidades futuras
- Restricciones del proyecto

---

## Patrones de Diseño Utilizados

- Dependency Injection
- Repository Pattern
- DTO Pattern
- Service Layer Pattern
- Builder Pattern
- Factory Method
- Singleton (gestionado por Spring)

## Requerimientos Funcionales

| ID | Requerimiento | Módulo |
|----|---------------|--------|
| R22 | Recibir alertas de sobrecarga y bajo rendimiento | notification-service |
| R23 | Ver sugerencia de qué estudiar hoy | notification-service |
| RF-01 | Login de usuarios | Seguridad |
| RF-02 | Gestión de datos | Core |



---

# Objetivos

## Objetivo General

Construir un sistema que ...

## Objetivos Específicos

- Objetivo 1
- Objetivo 2
- Objetivo 3

---

# Planteamiento del Problema

## Contexto

Descripción del contexto.

## Problema

Problema principal identificado.

#  Stack Tecnológico

| Área | Tecnologías |
|------|-------------|
| Backend | Java 21, Spring Boot 3 |
| Frontend | React / Angular |
| API | REST, OpenAPI, Swagger |
| Seguridad | Spring Security, JWT |
| SQL | PostgreSQL |
| NoSQL | MongoDB |
| Persistencia | Spring Data JPA |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, GitHub Actions |
| Calidad | SonarCloud, JaCoCo |
| Documentación | Swagger UI |

## Solución Propuesta

Descripción general del enfoque.

---

#  Requerimientos

---

## Requerimientos Funcionales

## Documentación de API

http://localhost:8080/swagger-ui.html

---

##  Análisis de Requerimientos

- [Documento de análisis](docs/requisitos.md)

---

#  Arquitectura

## Arquitectura General

Descripción de arquitectura usada:

- Monolítica / Microservicios
- Patrón MVC
- Clean Architecture
- Hexagonal (si aplica)

---

#  Stack Tecnológico

| Área | Tecnologías |
|------|-------------|
| Backend | Java 21, Spring Boot |
| Frontend | React / Angular |
| API | REST, OpenAPI |
| Seguridad | Spring Security, JWT |
| SQL | PostgreSQL |
| NoSQL | MongoDB |
| Testing | JUnit, Mockito |
| DevOps | Docker, GitHub Actions |
| Calidad | SonarCloud, JaCoCo |

---

#  Diagramas

## Contexto
- [Diagrama de contexto](docs/diagramas/contexto.png)

## Casos de Uso
- [Casos de uso](docs/diagramas/casos-uso.png)

## Diagrama de Clases
- [Diagrama de clases](docs/diagramas/clases.png)

## Componentes
- [Diagrama de componentes](docs/diagramas/componentes.png)

## Entidad Relación
- [ER Diagram](docs/diagramas/er.png)

## Secuencia

- [01 Registro usuario](docs/secuencia/registro.md)
- [02 Login](docs/secuencia/login.md)
- [03 Gestión principal](docs/secuencia/modulo.md)

---

#  Gestión del Proyecto

## Metodología

- Scrum

## Sprints

| Sprint | Objetivo | Estado |
|-------|----------|--------|
| Sprint 1 | Setup proyecto | ✅ |
| Sprint 2 | Core features | 🚧 |

## Riesgos

| Riesgo | Impacto | Mitigación |
|--------|---------|------------|
| Retrasos | Alto | Buffer |
| Bugs críticos | Medio | Testing |

---

#  Pruebas

## Estrategia

- Unitarias
- Integración
- End to End
- Carga

## Reporte

[Ver reporte pruebas](docs/testing/pruebas.md)

---

# Cobertura

Reporte generado con **JaCoCo** y analizado con **SonarCloud**

| Métrica | Cubierto | Total | Cobertura |
|---|---|---|---|
| Líneas | 1964 | 2188 | 90% |
| Ramas | 509 | 745 | 68% |
| Métodos | 744 | 794 | 94% |

## Calidad

- Bugs: 0 críticos
- Code Smells: X
- Deuda técnica: Baja
- Quality Gate: ✅ Passed

---

#  Demo

## Video Demo
- [Demo módulo](link-demo)

## Capturas

Agregar screenshots aquí.

---

# Instalación

## Requisitos

- Java 21
- Docker
- PostgreSQL

## Clonar repositorio

```bash
git clone https://github.com/usuario/proyecto.git
cd proyecto
```

## Backend

```bash
./mvnw spring-boot:run
```

## Frontend

```bash
npm install
npm run dev
```

---

# Estructura del Proyecto

```bash
src/
docs/
tests/
docker/
```

---

#  Referencias

- Documentación oficial Spring
- PostgreSQL docs
- OpenAPI
- Papers / fuentes usadas

---

