#!/bin/bash

# ==========================================
# Production Deployment Script for IoT Backend
# ==========================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="docker-compose.production.yml"
PROJECT_NAME="iot-backend-prod"
BACKUP_DIR="./backups"

# Functions
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_requirements() {
    print_info "Checking requirements..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    if [ ! -f "$COMPOSE_FILE" ]; then
        print_error "Docker Compose file '$COMPOSE_FILE' not found"
        exit 1
    fi
    
    print_success "All requirements satisfied"
}

backup_data() {
    print_info "Creating backup..."
    
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
    fi
    
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_FILE="$BACKUP_DIR/postgres_backup_$TIMESTAMP.sql"
    
    if docker ps | grep -q "${PROJECT_NAME}_postgres"; then
        docker exec "${PROJECT_NAME}_postgres_1" pg_dump -U iotuser iotdb > "$BACKUP_FILE"
        print_success "Database backup created: $BACKUP_FILE"
    else
        print_warning "PostgreSQL container not running, skipping backup"
    fi
}

deploy() {
    print_info "Starting production deployment..."
    
    # Pull latest images
    print_info "Pulling latest Docker images..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" pull
    
    # Build application
    print_info "Building application..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" build --no-cache
    
    # Start services
    print_info "Starting services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d
    
    # Wait for services
    print_info "Waiting for services to be ready..."
    sleep 30
    
    # Health check
    health_check
}

health_check() {
    print_info "Performing health check..."
    
    # Check PostgreSQL
    if docker exec "${PROJECT_NAME}_postgres_1" pg_isready -U iotuser -d iotdb &> /dev/null; then
        print_success "PostgreSQL is healthy"
    else
        print_error "PostgreSQL health check failed"
        show_logs "postgres"
        exit 1
    fi
    
    # Check Mosquitto
    if docker exec "${PROJECT_NAME}_mosquitto_1" mosquitto_pub -h localhost -t test -m "health_check" &> /dev/null; then
        print_success "Mosquitto is healthy"
    else
        print_warning "Mosquitto health check failed (may be normal)"
    fi
    
    # Check Spring Boot app
    sleep 20
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        print_success "Spring Boot application is healthy"
    else
        print_error "Spring Boot application health check failed"
        show_logs "app"
        exit 1
    fi
}

show_logs() {
    local service=$1
    print_info "Showing logs for $service..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs --tail=50 "$service"
}

stop_services() {
    print_info "Stopping services..."
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" down
    print_success "Services stopped"
}

cleanup() {
    print_info "Cleaning up unused Docker resources..."
    docker system prune -f
    docker volume prune -f
    print_success "Cleanup completed"
}

show_status() {
    print_info "Service status:"
    docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" ps
    
    print_info ""
    print_info "Service URLs:"
    echo "  - Spring Boot API: http://localhost:8080"
    echo "  - Health Check: http://localhost:8080/actuator/health"
    echo "  - MQTT Broker: mqtt://localhost:1883"
    echo "  - MQTT WebSocket: ws://localhost:9001"
    echo "  - PostgreSQL: localhost:5432"
}

# Main script
case "${1:-deploy}" in
    "deploy")
        check_requirements
        backup_data
        deploy
        show_status
        ;;
    "backup")
        check_requirements
        backup_data
        ;;
    "health")
        check_requirements
        health_check
        ;;
    "logs")
        if [ -n "$2" ]; then
            show_logs "$2"
        else
            docker-compose -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs -f
        fi
        ;;
    "stop")
        stop_services
        ;;
    "restart")
        stop_services
        deploy
        show_status
        ;;
    "status")
        show_status
        ;;
    "cleanup")
        cleanup
        ;;
    "help")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  deploy    - Deploy the application (default)"
        echo "  backup    - Create database backup"
        echo "  health    - Check service health"
        echo "  logs      - Show service logs"
        echo "  stop      - Stop all services"
        echo "  restart   - Restart all services"
        echo "  status    - Show service status"
        echo "  cleanup   - Clean up unused Docker resources"
        echo "  help      - Show this help message"
        ;;
    *)
        print_error "Unknown command: $1"
        print_info "Use '$0 help' for available commands"
        exit 1
        ;;
esac