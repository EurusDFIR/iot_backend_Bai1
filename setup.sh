#!/bin/bash

# 🚀 IoT Backend Quick Setup Script
# Chạy script này để setup tự động

echo "🚀 Starting IoT Backend Setup..."

# 1. Check Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker not found. Please install Docker first."
    exit 1
fi

# 2. Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install JDK 17+."
    exit 1
fi

echo "✅ Docker and Java found."

# 3. Start Docker services
echo "🐳 Starting Docker services..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# 4. Check if services are running
if ! docker-compose ps | grep -q "Up"; then
    echo "❌ Docker services failed to start."
    exit 1
fi

echo "✅ Docker services are running."

# 5. Try to run Spring Boot app
echo "🌱 Starting Spring Boot application..."

# Try different methods
if command -v mvn &> /dev/null; then
    echo "Using system Maven..."
    mvn spring-boot:run
elif [ -f "./mvnw" ]; then
    echo "Using Maven Wrapper..."
    chmod +x mvnw
    ./mvnw spring-boot:run
else
    echo "❌ No Maven found. Please install Maven or fix JAVA_HOME."
    echo "💡 Try: export JAVA_HOME=\"C:/Program Files/Eclipse Adoptium/jdk-17\""
    exit 1
fi