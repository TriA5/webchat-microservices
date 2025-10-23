@echo off
echo ========================================
echo Starting all services with Docker Compose...
echo ========================================
docker-compose up --build -d

echo.
echo ========================================
echo Services are starting...
echo ========================================
echo.
echo Eureka Server: http://localhost:8761
echo API Gateway:   http://localhost:8080
echo User Service:  http://localhost:8081
echo MySQL:         localhost:3308
echo.
echo Run 'docker-compose logs -f' to view logs
echo Run 'docker-compose down' to stop all services
pause
