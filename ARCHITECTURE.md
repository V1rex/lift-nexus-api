# 🏗️ System Architecture: Lift Nexus API

This document outlines the architectural boundaries, domain models, and strategic technical decisions behind the **Lift Nexus API** ecosystem.

---

## 1. Architectural Style & Philosophy

Lift Nexus is built as a **Modular Monolith** applying **Domain-Driven Design (DDD)** principles using a **Feature-as-Package (Screaming Architecture)** directory structure. 

Instead of organizing code by technical layers (e.g., `controllers/`, `services/`), the codebase is partitioned by business capabilities. This ensures high cohesion, strict domain encapsulation, and provides a clear extraction path into microservices as the system scales towards event-driven Sector Coupling.

```text
src/main/java/com/v1rex/liftnexus/
├── forklift/                    ← Fleet management bounded context
├── loadunit/                    ← Physical inventory bounded context
├── storagebin/                  ← Warehouse topology bounded context
├── transportorder/              ← Logistics bounded context
└── planning/                    ← Core constraint-solver and orchestration
```

---

## 2. Component Architecture (C4 Level)

The application enforces a strict **6-Layer Stack** within each bounded context. Inter-domain communication is strictly routed through **Service** interfaces to prevent database-level tight coupling.

```
┌─────────────────────────────────────────────────────────────────┐
│                        REST API Layer                           │
└────┬─────────────────────────────┬──────────────────────────────┘
     │                             │                
┌────▼─────────────┐   ┌───────────▼───────┐   ┌──────────────────┐
│  Forklift Domain │   │  Logistics Domain │   │ Topology Domain  │
│  ├─ Controller   │   │  ├─ Controller    │   │ ├─ Controller    │
│  ├─ Service      │◀──┼──┤  Service       ├───▶ ├─ Service       │
│  ├─ Repository   │   │  ├─ Repository    │   │ ├─ Repository    │
│  └─ Entity       │   │  └─ Entity        │   │ └─ Entity        │
└──────────────────┘   └───────────────────┘   └──────────────────┘
            ▲                                         ▲
            │       ┌─────────────────────────┐       │
            └───────┤     Planning Domain     ├───────┘
                    │ (Timefold Orchestrator) │
                    └─────────────────────────┘
```

**Key Boundaries:**

- **Slim Controllers:** Handle HTTP routing, payload validation (`@Valid`), and DTO translations only.
- **Fat Services:** Handle transaction boundaries (`@Transactional`), business rules, and cross-domain lookups.
- **Anti-Corruption Rule:** A Service in Domain A may never inject a Repository from Domain B. It must call the Service of Domain B.

---

## 3. Constraint System Design (The VRP Engine)

To prevent the Timefold `ConstraintProvider` from becoming an unmaintainable monolith, the optimization engine utilizes a **Modular Constraint Design**.

Each business constraint is isolated into its own testable class, making it trivial to toggle specific mathematical penalties (e.g., for A/B testing or future dynamic energy constraints).

```java
// planning/constraints/WarehouseConstraintProvider.java
public class WarehouseConstraintProvider implements ConstraintProvider {
    
    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
            ForkliftCapacityConstraint.enforceCapacity(factory),
            ForkliftTravelDistanceConstraint.minimizeDeadheading(factory),
            EquipmentRequirementConstraint.matchForkliftType(factory)
        };
    }
}
```

**Why this design:**
- ✅ One file = one constraint = one concern (Single Responsibility Principle)
- ✅ Easy to understand the business rule at a glance
- ✅ Testable: Mock ConstraintFactory, verify behavior
- ✅ Versioning: Can create CapacityConstraintV2 alongside V1
- ✅ A/B Testing: Wrap constraints in conditionals

---

## 4. Fat Service, Slim Controller Pattern

### **Slim Controllers – HTTP Translation Only**

```java
@PostMapping
public ResponseEntity<ForkliftResponse> createForklift(
        @RequestBody @Valid ForkliftRequest request) {
    ForkliftResponse saved = forkliftService.createForklift(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(saved.id())
        .toUri();
    return ResponseEntity.created(location).body(saved);
}
```

Controllers handle ONLY:
- HTTP status codes (201 Created, 400 Bad Request, etc.)
- Request/response DTO translation
- URL routing & header management
- Input validation (delegated framework)

### **Fat Services – Business Logic & Orchestration**

```java
@Service
@Transactional
public class ForkliftService {
    
    private final ForkliftRepository repo;
    private final StorageBinService binService;  // Cross-domain!
    
    public ForkliftResponse createForklift(ForkliftRequest request) {
        // Business validation
        if (repo.existsByFleetNumber(request.fleetNumber())) {
            throw new IllegalStateException("Fleet # already exists");
        }
        
        // Cross-domain coordination (via Service, NOT Repository!)
        StorageBin location = binService.findEntityById(request.binId());
        
        // Entity creation & persistence
        Forklift forklift = mapper.toEntity(request);
        forklift.setCurrentStorageBin(location);
        return mapper.toResponse(repo.save(forklift));
    }
}
```

Services handle:
- Transaction boundaries (`@Transactional`)
- Business rule validation
- Inter-domain orchestration (calls other Services)
- Exception handling & logging
- Entity lifecycle management

---

## 5. Cross-Domain Communication Rules

### **Rule 1: Services Only (Never Inject Repositories)**

```java
// ✅ Correct
class WarehouseDispatcherService {
    private final ForkliftService forkliftService;      // Cross-domain via Service
    private final TransportOrderService orderService;  // ✅
}

// ❌ Wrong
class WarehouseDispatcherService {
    private final ForkliftRepository repo;  // ❌ Direct repository access!
}
```

### **Rule 2: DTOs at Package Boundary**

Never expose entities across domain boundaries.

```java
// ✅ Correct
public ResponseEntity<ForkliftResponse> getForklift(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));  // DTO
}

// ❌ Wrong
public ResponseEntity<Forklift> getForklift(@PathVariable Long id) {
    return ResponseEntity.ok(forkliftService.findById(id));  // Entity exposed!
}
```

---



## 6. Evolution to Sector Coupling (Milestones 2 & 3)

The Lift Nexus architecture is intentionally designed to evolve from a centralized monolith into a distributed, event-driven Demand-Response ecosystem.

### **Milestone 2: Asynchronous Event-Driven Optimization**
To handle massive computational routing requests without blocking HTTP threads, the synchronous execution path will shift to an asynchronous queue model.

### **Milestone 3: External Energy Market Integration (`electron-nexus-api`)**

To achieve true sector coupling, the Java core will interface with a standalone Python service (electron-nexus-api).

---

## 7. Observed Architectural Issues & Future Refactoring

As the system transitions toward Milestone 2 and 3, two key structural limitations within the current architecture have been identified for future refactoring:

### **Issue 1: The Contaminated Entity Problem (JPA vs. Optimization Model)**

**Observed Issue:**  
Currently, core domain classes (like `Forklift`) serve a dual purpose. They carry relational database annotations (`@Entity`, `@Table`) as well as optimization metadata (`@PlanningEntity`, `@PlanningVariable`). This couples the mathematical VRP solver logic directly to the database schema.

**Risk:**
- Solver changes force database migrations
- Difficult to version the optimization model independently
- Hard to test pure domain logic without JPA overhead

**Target Solution (Post-M1):**  
Migration to **Hexagonal Architecture (Ports & Adapters)**. The database entity will be split:
- `ForkliftEntity` (Infrastructure Layer) – Contains JPA annotations only
- `Forklift` (Domain Core) – Pure Java model for Timefold, zero infrastructure coupling
- `ForkliftMapper` (Port) – Translates between layers at persistence boundary


### **Issue 2: Service Layer Bloat**

**Observed Issue:**  
Adhering to the "Slim Controller, Fat Service" pattern risks creating oversized Service classes as more complex business logic is added across multiple domains and transactions.

**Risk:**
- Single methods handling 10+ responsibilities
- Difficult unit testing due to high coupling
- Performance bottlenecks when write operations block read queries

**Target Solution (Milestone 2+):**  
Introduction of **CQRS (Command Query Responsibility Segregation)** pattern:
- **Write Path (Commands):** `ExecuteDispatchCommand` → `DispatchCommandHandler` → Updates state asynchronously via event streams
- **Read Path (Queries):** `GetFleetStatusQuery` → `FleetStatusQueryHandler` → Returns lightweight DTO immediately



---

**Last Updated:** 2026-05-31  
**Updated By:** Amine Bahij
**Status:** Milestone 1 (Static Dispatching MVP)  
**Next Evolution:** Milestone 2 (Real-Time Event-Driven)

