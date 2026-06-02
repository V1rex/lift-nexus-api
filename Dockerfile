# ============================================================
# Stage 1: Build the application
# ============================================================
# We use the official Maven image which bundles Eclipse Temurin
# JDK 21 with Maven 3.9 on Alpine Linux.
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /workspace

# Give Maven enough heap. The default is often too small for
# Spring Boot's dependency resolution and annotation processing.
ENV MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=256m"

# Layer caching trick: copy pom.xml FIRST so dependencies download
# into a cached layer.
COPY pom.xml ./

RUN mvn dependency:go-offline -B -DskipTests -Dspotless.check.skip=true -Dcheckstyle.skip=true
COPY src ./src

RUN mvn package -DskipTests -Dspotless.check.skip=true -Dcheckstyle.skip=true -B

# ============================================================
# Stage 2: Create the runtime image
# ============================================================
# JRE-only Alpine image (no JDK, no Maven). Shrinks the final
# image from ~600MB to ~200MB compared to using the builder image.
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL org.opencontainers.image.title="Lift Nexus API"
LABEL org.opencontainers.image.description="Async constraint-based optimization engine for warehouse dispatching"
LABEL org.opencontainers.image.authors="Mohamed Amine Bahij <medaminebahij02@gmail.com>"
LABEL org.opencontainers.image.url="https://github.com/v1rex/lift-nexus-api"
LABEL org.opencontainers.image.licenses="Apache-2.0"

# Security: run as non-root user, not root.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# --from=builder: this is the multi-stage magic. Only the JAR
# makes it to the final image. Everything else is discarded.
COPY --from=builder /workspace/target/*.jar app.jar

USER appuser

EXPOSE 8080

# exec form ["..."] ensures the JVM receives OS signals directly
# for graceful shutdown. -XX:+UseZGenerational enables ZGC
# generational mode for sub-millisecond pause times.
ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]