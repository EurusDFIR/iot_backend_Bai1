#!/bin/bash

echo "🚀 IoT Backend - Docker All-in-One Setup"
echo "========================================="

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker Desktop first."
    echo "🔗 Download: https://www.docker.com/products/docker-desktop"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo "❌ Docker not running. Please start Docker Desktop."
    exit 1
fi

echo "✅ Docker is ready"

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down -v

# Build and start all services
echo "🏗️  Building and starting all services..."
echo "   📦 PostgreSQL Database"
echo "   📡 Mosquitto MQTT Broker"  
echo "   🌱 Spring Boot IoT Backend"
echo ""
echo "⏳ This may take 2-3 minutes for first build..."

docker-compose up --build -d

# Wait for services to be healthy
echo "⏳ Waiting for all services to be ready..."

max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if docker-compose ps --filter status=running --filter health=healthy | grep -q "iot-backend-app"; then
        echo "✅ All services are running and healthy!"
        break
    fi
    
    echo "   Still waiting... ($((attempt + 1))/$max_attempts)"
    sleep 10
    attempt=$((attempt + 1))
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ Services took too long to start. Check logs:"
    echo "   docker-compose logs iot-backend"
    exit 1
fi

# Show status
echo ""
echo "🎉 SUCCESS! IoT Backend is running!"
echo "=================================="
echo "📱 Web API: http://localhost:8080"
echo "📊 Database: localhost:5432 (iotdb/iotuser/secret)"
echo "📡 MQTT: localhost:1883"
echo ""
echo "🧪 Quick test:"
echo "curl http://localhost:8080/api/devices"
echo ""
echo "🔧 Useful commands:"
echo "   View logs:    docker-compose logs -f"
echo "   Stop all:     docker-compose down"
echo "   Restart:      docker-compose restart"
echo "   Clean reset:  docker-compose down -v && docker-compose up -d"