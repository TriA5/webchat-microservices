# 🐳 Docker Setup Guide

## 📋 Yêu cầu hệ thống

- Docker Desktop đã cài đặt và đang chạy
- Java 17 (để build các service)
- Maven (hoặc sử dụng Maven Wrapper có sẵn)

## 🚀 Cách chạy trên Docker

### Bước 1: Build tất cả các services

Chạy script build:
```cmd
build-all.bat
```

Hoặc build thủ công từng service:
```cmd
cd eureka-server
mvnw.cmd clean package -DskipTests
cd ..

cd api-gateway
mvnw.cmd clean package -DskipTests
cd ..

cd user-service
mvnw.cmd clean package -DskipTests
cd ..
```

### Bước 2: Khởi động tất cả services

Chạy script docker-up:
```cmd
docker-up.bat
```

Hoặc chạy thủ công:
```cmd
docker-compose up --build -d
```

### Bước 3: Kiểm tra logs

```cmd
docker-logs.bat
```

Hoặc:
```cmd
docker-compose logs -f
```

### Bước 4: Dừng tất cả services

```cmd
docker-down.bat
```

Hoặc:
```cmd
docker-compose down
```

## 📊 Services URLs

| Service | URL | Port |
|---------|-----|------|
| Eureka Server | http://localhost:8761 | 8761 |
| API Gateway | http://localhost:8080 | 8080 |
| User Service | http://localhost:8081 | 8081 |
| MySQL | localhost:3308 | 3308 |

## 🔍 Kiểm tra health

### Eureka Server
```
http://localhost:8761/actuator/health
```

### User Service (qua API Gateway)
```
http://localhost:8080/api/users/actuator/health
```

## 📝 Lệnh Docker hữu ích

### Xem các container đang chạy
```cmd
docker ps
```

### Xem logs của một service cụ thể
```cmd
docker logs -f eureka-server
docker logs -f api-gateway
docker logs -f user-service
docker logs -f mysql-db
```

### Restart một service
```cmd
docker restart eureka-server
docker restart api-gateway
docker restart user-service
```

### Vào MySQL container
```cmd
docker exec -it mysql-db mysql -uroot -pTriduong123@
```

### Xóa tất cả (bao gồm volumes)
```cmd
docker-compose down -v
```

## 🔧 Troubleshooting

### Container không khởi động được

1. Kiểm tra logs:
```cmd
docker logs container-name
```

2. Kiểm tra port đã bị chiếm chưa:
```cmd
netstat -ano | findstr :8761
netstat -ano | findstr :8080
netstat -ano | findstr :8081
netstat -ano | findstr :3308
```

### Database không kết nối được

1. Kiểm tra MySQL container:
```cmd
docker ps | findstr mysql
```

2. Kiểm tra database đã tạo chưa:
```cmd
docker exec mysql-db mysql -uroot -pTriduong123@ -e "SHOW DATABASES;"
```

### Rebuild lại image

```cmd
docker-compose build --no-cache
docker-compose up -d
```

## 🎯 Test API

### User Service (qua API Gateway)

#### Tạo user mới
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User"
  }'
```

#### Lấy danh sách users
```bash
curl http://localhost:8080/api/users
```

#### Lấy user theo ID
```bash
curl http://localhost:8080/api/users/{userId}
```

## 📦 Cấu trúc Docker

```
webchat-microservices/
├── docker-compose.yml          # Docker Compose configuration
├── build-all.bat              # Build all services
├── docker-up.bat              # Start all services
├── docker-down.bat            # Stop all services
├── docker-logs.bat            # View logs
├── eureka-server/
│   ├── Dockerfile
│   └── target/eureka-server-1.0.0.jar
├── api-gateway/
│   ├── Dockerfile
│   └── target/api-gateway-0.0.1-SNAPSHOT.jar
└── user-service/
    ├── Dockerfile
    └── target/user-service-0.0.1-SNAPSHOT.jar
```

## 🔄 Profiles

Tất cả services sử dụng profile `docker` khi chạy trong container:
- Eureka URL: `http://eureka-server:8761/eureka/`
- MySQL URL: `jdbc:mysql://mysql:3306/db_user_service`

## ⚙️ Environment Variables

| Variable | Service | Value |
|----------|---------|-------|
| SPRING_PROFILES_ACTIVE | user-service | docker |
| SPRING_PROFILES_ACTIVE | api-gateway | docker |
| MYSQL_ROOT_PASSWORD | mysql | Triduong123@ |
| MYSQL_DATABASE | mysql | db_user_service |
