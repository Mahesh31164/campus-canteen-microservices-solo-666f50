# Campus Canteen Microservices — Starter Template

A beginner hackathon that takes you from an unconfigured Spring Boot multi-module
workspace to **five services** discovering each other through **Eureka** and
talking over a **four-hop request chain**. No database, no Docker, no message
broker — every hour goes into service communication.

## Prerequisites

- JDK 21 (only — no Maven, no Gradle install; use the shipped `./gradlew` wrapper)
- An IDE (IntelliJ or VS Code)
- Postman (optional, the collection is pre-written)

## The five services

| Service | Port  | Dependency                 | What it does                                  |
| --- | --- | --- | --- |
| `discovery-server` | 8761 | eureka-server              | Eureka registry — every service registers here |
| `api-gateway` | 8080 | gateway, eureka-client     | The only public door — routes to `lb://` targets |
| `auth-service` | 8083 | web, eureka-client         | Three hardcoded users, `Map<token, User>` |
| `menu-service` | 8081 | web, eureka-client, feign  | `MenuItem(id, name, price, stock)`, in memory |
| `order-service` | 8082 | web, eureka-client, feign  | In-memory orders, composes responses        |

**Deliberately empty:** every `application.yml` ships blank. With no `server.port`
anywhere, all four app services try to bind 8080 and the second one fails on
startup. **That failure is Ticket 1.** You configure the YAML, the annotations,
the controllers and the clients. Everything else — the module layout, the Gradle
build, the contracts module — ships complete and `./gradlew build` passes on clone.

## Project structure

```
canteen/
├── settings.gradle             modules
├── build.gradle                root build: Boot 3.5.3 + Spring Cloud 2025.0.0, Java 21
├── common-contracts/           empty dto package — 9 records go here (Ticket 4)
├── discovery-server/           eureka-server
├── api-gateway/                spring-cloud-gateway + eureka-client
├── auth-service/               web + eureka-client
├── menu-service/               web + eureka-client + openfeign
├── order-service/              web + eureka-client + openfeign
├── scripts/                    verify-setup, run-all, verify-T01..T10
├── tickets/                    T01..T10 — goal, steps, acceptance, 3 hints
└── postman/canteen.json        every request pre-written
```

## Quick start

```bash
./gradlew build            # whole reactor compiles (JDK 21 required)
scripts/verify-setup       # JDK, build, free ports — run before every event
scripts/run-all            # start all five services (Ticket 1: watch the clash)
scripts/verify-T01         # self-check each ticket before submitting
```

## Endpoints you must implement

Private (reachable only inside the network):

```
auth-service    POST /auth/login          { username, password } -> { token, userId, name }
auth-service    GET  /auth/validate       ?token= -> { userId, name } | 401
menu-service    GET  /menu                needs auth -> calls auth
menu-service    GET  /menu/{id}           internal, no token
menu-service    POST /menu/{id}/reserve   internal, { qty }
order-service   POST /orders              needs auth, { menuItemId, quantity }
order-service   GET  /orders/{id}         needs auth
order-service   GET  /orders/{id}/detail  needs auth, enriched response
```

Public (through the gateway, which is the only exposed port):

```
GET  /api/menu
GET  /api/orders/{id}
GET  /api/orders/{id}/detail
POST /api/orders
```

## The nine shared contracts (Ticket 4)

`LoginRequest`, `AuthResponse`, `ValidateResponse`, `MenuItem`, `ReserveRequest`,
`OrderRequest`, `Order`, `OrderDetailResponse`, `ApiResponse<T>` — all in
`common-contracts`, shared by the three service modules.

## License

Hackathon material for Archis Academy — internal use.