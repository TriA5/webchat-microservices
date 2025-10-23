@echo off
echo ========================================
echo Docker Services Status
echo ========================================
echo.

docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo ========================================
echo Service Health Check
echo ========================================
echo.

echo Checking Eureka Server...
curl -s http://localhost:8761/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo [OK] Eureka Server is running
) else (
    echo [ERROR] Eureka Server is not responding
)

echo.
echo Checking API Gateway...
curl -s http://localhost:8080/actuator/health 2>nul
if %errorlevel% equ 0 (
    echo [OK] API Gateway is running
) else (
    echo [ERROR] API Gateway is not responding
)

echo.
echo ========================================
pause
