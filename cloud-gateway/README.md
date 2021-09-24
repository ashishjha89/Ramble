### Api Gateway Hystrix stream

We can monitor it on:

```
http://localhost:9191/actuator/hystrix.stream
```

## Responsibilities

The responsibilities of this service is to:

- Route to different (load-balanced) services based on request uri (predicates). This allows callers of different
  services' api to use one port number (of api gateway).
- Adds Circuit Breaker (Hystrix) with timeout for service calls. It hosts fallback method which is called in case of
  timeout errors.