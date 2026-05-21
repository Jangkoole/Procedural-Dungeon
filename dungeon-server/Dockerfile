# ============================================================
# Stage 1: Build backend (Spring Boot jar)
# ============================================================
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /build
COPY dungeon-server/pom.xml ./
RUN apk add --no-cache maven && mvn dependency:resolve -q
COPY dungeon-server/src ./src
RUN mvn package -DskipTests -q

# ============================================================
# Stage 2: Build frontend (React / Vite static files)
# ============================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /build
COPY dungeon-web/ .
RUN npm install && npm run build

# ============================================================
# Stage 3: Runtime — Nginx + JRE
# ============================================================
FROM nginx:alpine

# Install JRE for backend
RUN apk add --no-cache openjdk17-jre

# Copy backend jar
COPY --from=backend-builder /build/target/*.jar /app/app.jar

# Copy frontend static files
COPY --from=frontend-builder /build/dist /usr/share/nginx/html

# Nginx config: proxy /api/ to backend on localhost:4010
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html; \
        try_files $uri $uri/ /index.html; \
    } \
    location /api/ { \
        proxy_pass http://127.0.0.1:4010; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
    } \
}' > /etc/nginx/conf.d/default.conf

# Startup script: launch backend then nginx
RUN echo '#!/bin/sh \
    java -jar /app/app.jar & \
    nginx -g "daemon off;"' > /start.sh && chmod +x /start.sh

EXPOSE 80

CMD ["/start.sh"]
