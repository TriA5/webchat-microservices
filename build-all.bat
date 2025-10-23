@echo off
echo ========================================
echo Building all microservices...
echo ========================================

echo.
echo [1/3] Building Eureka Server...
cd eureka-server
call mvnw.cmd clean package -DskipTests
cd ..

echo.
echo [2/3] Building API Gateway...
cd api-gateway
call mvnw.cmd clean package -DskipTests
cd ..

echo.
echo [3/3] Building User Service...
cd user-service
call mvnw.cmd clean package -DskipTests
cd ..

echo.
echo ========================================
echo Build completed successfully!
echo ========================================
echo.
echo Now you can run: docker-compose up --build
pause
