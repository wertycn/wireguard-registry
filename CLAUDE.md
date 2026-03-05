# CLAUDE.md — WireGuard Registry

## Project Overview

Cross-cloud WireGuard network configuration generator and registry system. Manages WireGuard networking topologies across multiple clouds, LANs, and relay scenarios. Provides centralized node registration, configuration distribution, and real-time change notifications.

**Language:** Java 17
**Framework:** Spring Boot 3.2.2
**Build tool:** Maven (with Maven Wrapper `./mvnw`)
**License:** Apache 2.0

## Repository Structure

```
wireguard-registry/
├── wireguard-registry-core/       # Core library: config generation, models, storage, auth
├── wireguard-registry-service/    # REST API server (Spring Boot application)
├── wireguard-registry-client/     # Client library for registry interaction
├── deploy/                        # Dockerfile and bootstrap script
├── examples/                      # Deployment examples (cluster-deployment.md)
└── .github/workflows/             # CI/CD (GitHub Actions)
```

### Module Dependencies

- `wireguard-registry-service` → depends on `wireguard-registry-core`
- `wireguard-registry-client` → depends on `wireguard-registry-core`

### Key Packages (under `icu.debug.net.wg`)

| Package | Purpose |
|---------|---------|
| `core.model` | Data models for networks, nodes, interfaces, peers, configs |
| `core.model.config` | WireGuard configuration models |
| `core.registry` | `ConfigRegistry` interface and `DefaultConfigRegistry` implementation |
| `core.storage` | `ConfigStorage` with Memory, Database implementations |
| `core.auth` | Dual auth: `NodeAuthService` (Curve25519), `AdminAuthService` (JWT) |
| `core.helper` | Key generation, file utilities, network address allocation |
| `service.controller` | REST controllers: `RegistryController`, `AdminController`, `GenerateController` |
| `client` | `RegistryClient` for node-to-registry communication |

## Build & Test Commands

```bash
# Full build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run tests for a specific module
./mvnw test -pl wireguard-registry-core

# Run tests with JaCoCo coverage (as CI does)
./mvnw org.jacoco:jacoco-maven-plugin:0.8.10:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.10:report

# Run the service locally
./mvnw spring-boot:run -pl wireguard-registry-service
```

**Note:** Maven may need network access to resolve dependencies. If offline, builds will fail on dependency resolution.

## Configuration

### Application Profiles (`wireguard-registry-service/src/main/resources/application.yml`)

- **Default**: H2 in-memory database, standalone mode, port 8080
- **`memory`**: Volatile in-memory storage
- **`mysql`**: MySQL 8.0 backend (cluster-ready)
- **`mongodb`**: MongoDB backend (cluster-ready)
- **`sqlite`**: SQLite embedded database

### Deployment Modes

- **`standalone`**: Single instance, any storage backend
- **`cluster`**: Multiple stateless nodes with shared MySQL or MongoDB

### Key Defaults

- Server port: `8080`
- JSON naming: `SNAKE_CASE` (Jackson PropertyNamingStrategy)
- Timezone: `GMT+8`
- Default admin: `admin` / `admin123`
- JWT temp key expiry: 300s, signature expiry: 60s

## Code Conventions

### Style

- Java 17 features allowed
- **Lombok** heavily used: `@Slf4j`, `@Data`, `@Builder`, `@SneakyThrows`
- `lombok.config` present at project root (stops bubbling, adds `@Generated` annotations)
- Package naming: `icu.debug.net.wg.[module].[subpackage]`
- JSON serialization uses `SNAKE_CASE` — Java fields are camelCase, API responses are snake_case

### Annotations in Use

- **Spring**: `@RestController`, `@RequestMapping`, `@Service`, `@Repository`, `@Configuration`, `@Component`
- **JPA**: `@Entity`, `@Table`, `@Id`, `@Column`
- **Jackson**: `@JsonIgnore`, `@JsonProperty`
- **Logging**: SLF4J via Lombok's `@Slf4j`

### REST API Paths

- `/v1/registry/**` — Node registration, config retrieval, heartbeat
- `/v1/admin/**` — Admin user management, authentication, temp key generation

## Authentication Architecture

Dual authentication system:

1. **Node Authentication** — Curve25519 elliptic curve keypair signatures
   - Temp keys for initial registration (5 min validity)
   - Timestamp-based replay prevention (60s window)
2. **Admin Authentication** — JWT tokens with BCrypt password hashing
   - 5 roles: `SUPER_ADMIN`, `NETWORK_ADMIN`, `NODE_ADMIN`, `MONITOR`, `READ_ONLY`

See `AUTHENTICATION.md` and `AUTHENTICATION_SUMMARY.md` for full details.

## Testing

- **Framework**: JUnit 5 (Jupiter)
- **Test data**: JSON fixture files in `wireguard-registry-core/src/test/resources/`
  - `wireguard-network-example.json`, `*-v2.json`, `*-v3.json`, `*-keepalive.json`
- Tests cover: config generation, key generation, network address allocation, peer/interface models

## CI/CD

GitHub Actions workflow (`.github/workflows/maven.yml`):
- Triggers on push to `main` branch only
- JDK 17 on ubuntu-latest
- Steps: test with JaCoCo → SonarCloud scan → package → Docker build & push
- Docker images tagged with `latest` and commit SHA

## Documentation Reference

| File | Content |
|------|---------|
| `README.md` | Project overview and quick start (Chinese) |
| `REGISTRY.md` | Registry system architecture and API reference |
| `AUTHENTICATION.md` | Full authentication architecture |
| `AUTHENTICATION_SUMMARY.md` | Auth implementation summary |
| `CLUSTER_DESIGN.md` | Cluster architecture design |
| `CLUSTER_SIMPLIFICATION.md` | Simplified cluster rationale |
| `examples/cluster-deployment.md` | 3-node cluster setup with docker-compose |
