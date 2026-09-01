# Spring Boot Microservices Architecture

A demonstration of microservices architecture using Spring Boot and Spring Cloud Netflix stack, implementing a movie catalog system with service discovery and inter-service communication.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [Discovery Server](#1-discovery-server)
  - [Movie Catalog Service](#2-movie-catalog-service)
  - [Movie Info Service](#3-movie-info-service)
  - [Ratings Data Service](#4-ratings-data-service)
- [Service Interactions](#service-interactions)
- [API Documentation](#api-documentation)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Automated Tests](#automated-tests)
- [Service Configuration](#service-configuration)

---

## 🏗️ Architecture Overview

This project demonstrates a microservices architecture with the following components:

```
┌─────────────────────────────────────────────────────────────────┐
│                      Discovery Server                           │
│                   (Netflix Eureka Server)                       │
│                        Port: 8761                               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Service Registration
                ┌────────────┼────────────┐
                │            │            │
        ┌───────▼──────┐ ┌──▼──────────┐ ┌▼───────────────┐
        │   Movie      │ │   Movie     │ │   Ratings      │
        │   Catalog    │ │   Info      │ │   Data         │
        │   Service    │ │   Service   │ │   Service      │
        │  Port: 8081  │ │ Port: 8082  │ │  Port: 8083    │
        └──────┬───────┘ └──────▲──────┘ └────▲───────────┘
               │                │              │
               │   REST API     │              │
               └────────────────┴──────────────┘
                    (via Service Discovery)
```

### Key Architectural Patterns

1. **Service Discovery**: Netflix Eureka for dynamic service registration and discovery
2. **Client-Side Load Balancing**: Using Spring Cloud LoadBalancer with RestTemplate
3. **Inter-Service Communication**: RESTful APIs with service-to-service calls
4. **Aggregation Pattern**: Movie Catalog Service aggregates data from multiple services

---

## 🔧 Services

### 1. Discovery Server

**Purpose**: Service registry that enables service discovery using Netflix Eureka.

**Port**: 8761

**Responsibilities**:
- Maintains a registry of all microservice instances
- Provides service location information to clients
- Enables dynamic scaling and load balancing
- Health checking of registered services

**Technology**:
- Spring Cloud Netflix Eureka Server
- `@EnableEurekaServer` annotation

**Configuration Highlights**:
```properties
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

**Access**:
- Eureka Dashboard: `http://localhost:8761`

---

### 2. Movie Catalog Service

**Purpose**: Aggregator service that provides personalized movie catalogs for users by combining data from Movie Info and Ratings services.

**Port**: 8081

**Application Name**: `movie-catalog-service`

**Responsibilities**:
- Acts as an API Gateway/Aggregator for client applications
- Fetches user ratings from Ratings Data Service
- Fetches movie details from Movie Info Service
- Combines data to create personalized movie catalogs
- Implements client-side load balancing

**Key Components**:

- **RestTemplate**: Configured with `@LoadBalanced` for service discovery and load balancing
- **WebClient**: Reactive HTTP client (configured but not currently used in the implementation)

**Dependencies**:
- Movie Info Service (for movie details)
- Ratings Data Service (for user ratings)
- Discovery Server (for service discovery)

---

### 3. Movie Info Service

**Purpose**: Provides detailed information about movies.

**Port**: 8082

**Application Name**: `movie-info-service`

**Responsibilities**:
- Stores and serves movie metadata (title, description)
- Returns movie information by movie ID
- Currently uses in-memory data store (hardcoded HashMap)

**Data Store** (Sample Data):
```
Movie ID: 100 - Transformers (SciFi movie)
Movie ID: 101 - Titanic (Romantic movie)
Movie ID: 102 - Silent Place (Horror movie)
```

---

### 4. Ratings Data Service

**Purpose**: Manages movie ratings provided by users.

**Port**: 8083

**Application Name**: `ratings-data-service`

**Responsibilities**:
- Stores and serves user ratings for movies
- Provides ratings by movie ID
- Provides all ratings for a specific user
- Currently uses in-memory data store (hardcoded HashMap)

**Data Store** (Sample Data):
```
User 1000:
  - Movie 100: Rating 5
  - Movie 101: Rating 5

User 1001:
  - Movie 100: Rating 5
  - Movie 101: Rating 4
  - Movie 102: Rating 3
```

---

## 🔄 Service Interactions

### Request Flow for Getting User's Movie Catalog

```
Client
  │
  │ GET /catalog/{userId}
  ▼
┌─────────────────────────┐
│ Movie Catalog Service   │
│     (Port: 8081)        │
└───────┬─────────────┬───┘
        │             │
        │             │ Step 1: Get user ratings
        │             │ GET /ratings/user/{userId}
        │             ▼
        │      ┌─────────────────────────┐
        │      │ Ratings Data Service    │
        │      │     (Port: 8083)        │
        │      └─────────────────────────┘
        │             │
        │             │ Returns: UserRating object
        │             │ { userId, List<Rating> }
        │             │
        │◄────────────┘
        │
        │ Step 2: For each rating, get movie details
        │ GET /movies/{movieId}
        ▼
┌─────────────────────────┐
│ Movie Info Service      │
│     (Port: 8082)        │
└─────────────────────────┘
        │
        │ Returns: Movie object
        │ { movieId, name, description }
        │
        ▼
┌─────────────────────────┐
│ Movie Catalog Service   │
│  (Aggregates data)      │
└────────┬────────────────┘
         │
         │ Returns: List<CatalogItem>
         │ { name, description, rating }
         ▼
       Client
```

### Service Discovery Flow

1. **Startup**: Each service registers itself with the Discovery Server (Eureka)
2. **Service Lookup**: Movie Catalog Service queries Eureka for service locations
3. **Load Balancing**: RestTemplate with `@LoadBalanced` automatically distributes requests
4. **Resilience**: Eureka maintains heartbeats and updates service registry dynamically

### Communication Mechanism

- **Protocol**: HTTP/REST
- **Service Resolution**: Logical service names (e.g., `http://movie-info-service`) are resolved to actual host:port by Eureka client
- **Load Balancing**: Client-side load balancing using Spring Cloud LoadBalancer
- **HTTP Client**: RestTemplate with Apache HttpComponents (3-second connection timeout)

---

## 📚 API Documentation

### Movie Catalog Service APIs

#### Get User's Movie Catalog

**Endpoint**: `GET /catalog/{userId}`

**Description**: Retrieves a personalized movie catalog for a specific user, including movie details and their ratings.

**Path Parameters**:
- `userId` (String): Unique identifier of the user

**Response**: `200 OK`
```json
[
  {
    "name": "Transformers",
    "description": "Transformers is a SciFi movie",
    "rating": 5
  },
  {
    "name": "Titanic",
    "description": "Titanic is a romantic movie",
    "rating": 5
  }
]
```

**Response Model**: `CatalogItem[]`
- `name` (String): Movie title
- `description` (String): Movie description
- `rating` (int): User's rating for the movie

**Example**:
```bash
curl http://localhost:8081/catalog/1000
```

**Internal Flow**:
1. Calls Ratings Data Service to get user's ratings
2. For each rated movie, calls Movie Info Service to get movie details
3. Aggregates the data into CatalogItem objects

---

### Movie Info Service APIs

#### Get Movie Information

**Endpoint**: `GET /movies/{movieId}`

**Description**: Retrieves detailed information about a specific movie.

**Path Parameters**:
- `movieId` (String): Unique identifier of the movie

**Response**: `200 OK`
```json
{
  "movieId": "100",
  "name": "Transformers",
  "description": "Transformers is a SciFi movie"
}
```

**Response Model**: `Movie`
- `movieId` (String): Unique movie identifier
- `name` (String): Movie title
- `description` (String): Movie description

**Example**:
```bash
curl http://localhost:8082/movies/100
```

**Available Movies**:
- `100`: Transformers
- `101`: Titanic
- `102`: Silent Place

---

### Ratings Data Service APIs

#### Get Rating by Movie ID

**Endpoint**: `GET /ratings/{movieId}`

**Description**: Retrieves rating information for a specific movie.

**Path Parameters**:
- `movieId` (String): Unique identifier of the movie

**Response**: `200 OK`
```json
{
  "movieId": "100",
  "userId": "1000",
  "rating": 5
}
```

**Response Model**: `Rating`
- `movieId` (String): Unique movie identifier
- `userId` (String): User who provided the rating
- `rating` (int): Rating value

**Example**:
```bash
curl http://localhost:8083/ratings/100
```

---

#### Get All Ratings by User ID

**Endpoint**: `GET /ratings/user/{userId}`

**Description**: Retrieves all movie ratings provided by a specific user.

**Path Parameters**:
- `userId` (String): Unique identifier of the user

**Response**: `200 OK`
```json
{
  "userId": "1000",
  "ratings": [
    {
      "movieId": "100",
      "userId": "1000",
      "rating": 5
    },
    {
      "movieId": "101",
      "userId": "1000",
      "rating": 5
    }
  ]
}
```

**Response Model**: `UserRating`
- `userId` (String): Unique user identifier
- `ratings` (Rating[]): List of all ratings by the user
  - `movieId` (String): Movie identifier
  - `userId` (String): User identifier
  - `rating` (int): Rating value

**Example**:
```bash
curl http://localhost:8083/ratings/user/1000
```

**Available Users**:
- `1000`: Has rated movies 100 and 101
- `1001`: Has rated movies 100, 101, and 102

---

## 🛠️ Technology Stack

### Core Technologies

- **Java**: Version 20
- **Spring Boot**: 3.0.5
- **Spring Cloud**: 2022.0.2
- **Maven**: Build and dependency management

### Spring Cloud Components

- **Netflix Eureka**: Service discovery and registration
- **Spring Cloud Netflix Eureka Client**: Service discovery client
- **Spring Cloud LoadBalancer**: Client-side load balancing

### HTTP Clients

- **RestTemplate**: Synchronous HTTP client with load balancing
- **WebClient**: Reactive HTTP client (Spring WebFlux)
- **Apache HttpComponents**: HTTP client implementation

### Utilities

- **Lombok**: Reduces boilerplate code with annotations
- **Log4j2**: Logging framework

---

## 🚀 Getting Started

### Prerequisites

- Java 20 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Running the Services

**Important**: Services must be started in the following order to ensure proper registration and communication.

#### 1. Start Discovery Server

```bash
cd discovery-server
./mvnw spring-boot:run
```

Wait for the server to start completely. Access Eureka Dashboard at: `http://localhost:8761`

#### 2. Start Movie Info Service

```bash
cd movie-info-service
./mvnw spring-boot:run
```

Verify registration in Eureka Dashboard.

#### 3. Start Ratings Data Service

```bash
cd ratings-data-service
./mvnw spring-boot:run
```

Verify registration in Eureka Dashboard.

#### 4. Start Movie Catalog Service

```bash
cd movie-catalog-service
./mvnw spring-boot:run
```

Verify registration in Eureka Dashboard.

### Building All Services

```bash
# From each service directory
./mvnw clean install
```

### Testing the Application

Once all services are running, test the movie catalog endpoint:

```bash
# Get catalog for user 1000
curl http://localhost:8081/catalog/1000

# Get catalog for user 1001
curl http://localhost:8081/catalog/1001
```

Expected Response for user 1000:
```json
[
  {
    "name": "Transformers",
    "description": "Transformers is a SciFi movie",
    "rating": 5
  },
  {
    "name": "Titanic",
    "description": "Titanic is a romantic movie",
    "rating": 5
  }
]
```

---

## 🧪 Automated Tests

The manual `curl` walkthrough above verifies the running system end-to-end;
this section covers the automated test suite that runs without starting
any services.

```bash
# From each service directory
mvn test
```

`discovery-server` keeps its generated `contextLoads()` test — it's a
Eureka server bootstrap with no business logic of its own to test beyond
"does the Spring context start." The other three services now have real
tests alongside their `contextLoads()` checks:

| Service | Test class | Covers |
|---|---|---|
| `movie-info-service` | `MovieResourceTest` | Each hardcoded movie is returned correctly; an unknown movie ID returns an empty 200 body (documented current behavior, not a 404) |
| `ratings-data-service` | `RatingsResourceTest` | Per-user rating lookups return the right list in order; an unknown user gets a null `ratings` field; documents that `getRatingByMovieId` returns the same hardcoded rating regardless of which movie is requested |
| `movie-catalog-service` | `MovieCatalogResourceTest` | **The service's actual point** — the fan-out that calls ratings-data-service, then movie-info-service per rating, and assembles the catalog. Mocks `RestTemplate` (no Eureka needed) and covers the happy path, an empty-ratings response, and a rating whose movie lookup comes back empty (the controller's `if (movie != null)` guard should drop it) |

All three use `@WebMvcTest` — a fast slice test covering just the
controller and its mocked collaborators, not the full Spring context (so
no Eureka client, no "connection refused to localhost:8761" log noise
that `@SpringBootTest` produces when no discovery server is running).

Each real test was verified to actually catch a bug, not just pass by
construction: temporarily broke the corresponding production code
(flipped `movie-catalog-service`'s null-check, changed a hardcoded
description) and confirmed the right test failed, then reverted.

### A build fix you'll need if you're on a current JDK

All three Lombok-using services (`movie-catalog-service`,
`movie-info-service`, `ratings-data-service`) pin
`<lombok.version>1.18.34</lombok.version>` in their `pom.xml`. Without
it, `mvn test`/`mvn compile` fails on **JDK 21+** with:

```
NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport does not have member field 'qualid'
```

The Lombok version `spring-boot-starter-parent:3.0.5` manages predates
Lombok's JDK 21 support (added in 1.18.30). If you're building with JDK
17 or 20 this override is a no-op; it only matters on 21+.

---

## ⚙️ Service Configuration

### Discovery Server Configuration

**File**: `discovery-server/src/main/resources/application.properties`

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

- Runs on port 8761
- Does not register itself with Eureka
- Does not fetch registry from other Eureka servers

---

### Movie Catalog Service Configuration

**File**: `movie-catalog-service/src/main/resources/application.properties`

```properties
spring.application.name=movie-catalog-service
server.port=8081
```

- Service name used for discovery: `movie-catalog-service`
- REST API port: 8081
- Automatically registers with Eureka

**Key Configurations**:
- RestTemplate with `@LoadBalanced` for service discovery
- Connection timeout: 3000ms (3 seconds)
- WebClient builder for reactive calls (future use)

---

### Movie Info Service Configuration

**File**: `movie-info-service/src/main/resources/application.properties`

```properties
spring.application.name=movie-info-service
server.port=8082
```

- Service name used for discovery: `movie-info-service`
- REST API port: 8082
- Automatically registers with Eureka

---

### Ratings Data Service Configuration

**File**: `ratings-data-service/src/main/resources/application.properties`

```properties
spring.application.name=ratings-data-service
server.port=8083
```

- Service name used for discovery: `ratings-data-service`
- REST API port: 8083
- Automatically registers with Eureka

---

## 📊 Data Models

### CatalogItem (Movie Catalog Service)

Represents a movie in a user's personalized catalog.

```java
{
  "name": String,        // Movie title
  "description": String, // Movie description
  "rating": int         // User's rating
}
```

### Movie (Shared Model)

Represents movie information.

```java
{
  "movieId": String,     // Unique identifier
  "name": String,        // Movie title
  "description": String  // Movie description
}
```

### Rating (Shared Model)

Represents a single movie rating by a user.

```java
{
  "movieId": String,  // Movie identifier
  "userId": String,   // User identifier
  "rating": int       // Rating value (1-5)
}
```

### UserRating (Shared Model)

Represents all ratings provided by a user.

```java
{
  "userId": String,        // User identifier
  "ratings": Rating[]      // List of ratings
}
```

---

## 🔍 Key Features

### Service Discovery

- **Automatic Registration**: All services register with Eureka on startup
- **Health Monitoring**: Eureka tracks service health via heartbeats
- **Dynamic Scaling**: New instances are automatically discovered
- **Fault Tolerance**: Failed instances are removed from registry

### Load Balancing

- **Client-Side**: RestTemplate with `@LoadBalanced` annotation
- **Algorithm**: Default Round-robin (configurable)
- **No Single Point of Failure**: Load balancing logic distributed to clients

### Inter-Service Communication

- **Service Names**: Logical names resolve to actual endpoints
- **RESTful APIs**: Standard HTTP/JSON communication
- **Timeouts**: Configured connection timeouts prevent hanging requests
- **Loose Coupling**: Services only know service names, not locations

---

## 🔮 Future Enhancements

1. **Circuit Breaker**: Add Resilience4j for fault tolerance
2. **API Gateway**: Implement Spring Cloud Gateway for unified entry point
3. **Configuration Server**: Centralized configuration management
4. **Distributed Tracing**: Add Sleuth and Zipkin for request tracing
5. **Database Integration**: Replace in-memory data with persistent storage
6. **Security**: Implement OAuth2/JWT authentication
7. **Caching**: Add Redis for caching frequently accessed data
8. **Message Queue**: Implement async communication with RabbitMQ/Kafka
9. **Monitoring**: Add Spring Boot Actuator and Prometheus/Grafana
10. **Containerization**: Docker and Kubernetes deployment

---

## 📝 Notes

- All services currently use in-memory data stores for demonstration purposes
- The Movie Catalog Service demonstrates the aggregator pattern
- Load balancing is enabled but requires multiple instances to see its effect
- All inter-service calls use service names, not hardcoded URLs
- Connection timeout is set to 3 seconds to prevent long waits

---

## 📄 License

This project is for educational and demonstration purposes.

---

## 👥 Author

**Package**: io.emkae

For more information or questions about this microservices implementation, please refer to the individual service source code.
