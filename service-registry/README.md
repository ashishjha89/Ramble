### Eureka Service Registry
It runs on:
```
http://localhost:8761/
```

Note: Start Eureka Service Registry before your running your services.

### Responsibilities

This service is responsible for following:

- This service act as Eureka Server. It registers other services (implemented as Eureka Client).
- Through Eureka Server dashboard, we can see service health related metrics.