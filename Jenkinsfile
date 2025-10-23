pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        INFRASTRUCTURE_JOB = 'Infrastructure'
        MICROSERVICE_PORT = '8081'
        REQUIRED_INFRA_SERVICES = 'shared-mysql-db,kafka'
    }

    stages {
        stage('Checkout Microservice') {
            steps {
                checkout scm
            }
        }

        stage('Validate Project') {
            steps {
                script {
                    validateProjectStructure()
                }
            }
        }

        stage('Check Infrastructure Status') {
            steps {
                script {
                    env.INFRA_STATUS = checkInfrastructureStatus()
                    echo "Infrastructure Status: ${env.INFRA_STATUS}"

                    // Determine if we need to deploy infrastructure
                    env.NEEDS_INFRA_DEPLOYMENT = needsInfrastructureDeployment()
                    echo "Needs infrastructure deployment: ${env.NEEDS_INFRA_DEPLOYMENT}"
                }
            }
        }

        stage('Deploy Infrastructure If Needed') {
            when {
                expression { env.NEEDS_INFRA_DEPLOYMENT.toBoolean() }
            }
            steps {
                script {
                    echo "Infrastructure is not running or unhealthy - deploying..."
                    def infraResult = deployInfrastructure()
                    if (!infraResult.success) {
                        error "Infrastructure deployment failed: ${infraResult.error}"
                    }
                    echo "✅ Infrastructure deployed successfully"
                }
            }
        }

        stage('Use Existing Infrastructure') {
            when {
                expression { !env.NEEDS_INFRA_DEPLOYMENT.toBoolean() }
            }
            steps {
                script {
                    echo "✅ Infrastructure is already running and healthy"
                    echo "Proceeding with microservice deployment using existing infrastructure"
                }
            }
        }

        stage('Build Application') {
            steps {
                script {
                    buildApplication()
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    buildDockerImage()
                }
            }
        }

        stage('Deploy Microservice') {
            steps {
                script {
                    deployMicroservice()
                }
            }
        }

        stage('Verify Microservice Health') {
            steps {
                script {
                    verifyMicroserviceHealth()
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            script {
                echo "🚀 Microservice deployment completed successfully!"
                echo "Access your service: http://localhost:${env.MICROSERVICE_PORT}"
                echo "Health check: http://localhost:${env.MICROSERVICE_PORT}/actuator/health"
            }
        }
        failure {
            echo "❌ Microservice deployment failed!"
        }
    }
}

// ========== INFRASTRUCTURE CHECK FUNCTIONS ==========

def checkInfrastructureStatus() {
    echo "Checking infrastructure status (no port exposure)..."

    try {
        // Get all running containers
        def runningContainers = sh(
            script: "docker ps --format '{{.Names}}'",
            returnStdout: true
        ).trim()

        if (runningContainers == '') {
            return "not-running"
        }

        def containerList = runningContainers.split('\n')
        def requiredServices = env.REQUIRED_INFRA_SERVICES.split(',')

        // Check if all required services are running
        def missingServices = requiredServices.findAll { !containerList.contains(it) }

        if (missingServices) {
            return "partial:missing-${missingServices.join(',')}"
        }

        // Check if services are healthy using internal container checks
        def healthStatus = checkInfrastructureHealth(requiredServices)
        if (!healthStatus.healthy) {
            return "running-but-unhealthy:${healthStatus.unhealthyServices.join(',')}"
        }

        return "healthy"

    } catch (Exception e) {
        echo "Error checking infrastructure status: ${e.getMessage()}"
        return "error"
    }
}

def checkInfrastructureHealth(services) {
    def unhealthyServices = []

    services.each { service ->
        try {
            switch(service) {
                case 'shared-mysql-db':
                    // Check MySQL using internal container command (no port exposure)
                    sh '''
                        # Check if MySQL process is running and can accept connections internally
                        if docker exec shared-mysql-db mysqladmin ping -h localhost --silent 2>/dev/null; then
                            echo "MySQL is healthy"
                        else
                            # Alternative: check if MySQL process is running
                            docker exec shared-mysql-db ps aux | grep -q "[m]ysqld" || exit 1
                        fi
                    '''
                    break

                case 'kafka':
                    // Check Kafka using internal container checks (no port exposure)
                    sh '''
                        # Method 1: Check if Kafka process is running
                        if docker exec kafka ps aux | grep -q "[k]afka"; then
                            echo "Kafka process is running"
                        else
                            # Method 2: Check if Kafka started successfully by examining logs
                            if docker logs kafka --tail 10 2>/dev/null | grep -q "started"; then
                                echo "Kafka started successfully"
                            else
                                # Method 3: Check if the container has been running long enough to be initialized
                                container_uptime=$(docker inspect --format='{{.State.StartedAt}}' kafka 2>/dev/null | xargs -I {} date -d {} +%s)
                                current_time=$(date +%s)
                                if [ $((current_time - container_uptime)) -gt 60 ]; then
                                    echo "Kafka container running long enough - assuming healthy"
                                else
                                    exit 1
                                fi
                            fi
                        fi
                    '''
                    break

                default:
                    // For other services, just check if container is running
                    sh "docker ps | grep -q ${service}"
            }
        } catch (Exception e) {
            echo "Service ${service} is unhealthy: ${e.getMessage()}"
            unhealthyServices.add(service)
        }
    }

    return [healthy: unhealthyServices.isEmpty(), unhealthyServices: unhealthyServices]
}

def needsInfrastructureDeployment() {
    def status = env.INFRA_STATUS
    echo "Current infrastructure status: ${status}"

    switch(status) {
        case 'not-running':
            echo "Infrastructure not running - deployment needed"
            return true

        case 'error':
            echo "Error checking infrastructure - deployment needed for safety"
            return true

        case ~/^partial:missing-.*/:
            def missingServices = status.replace('partial:missing-', '').split(',')
            echo "Missing services: ${missingServices.join(', ')} - deployment needed"
            return true

        case ~/^running-but-unhealthy:.*/:
            def unhealthyServices = status.replace('running-but-unhealthy:', '').split(',')
            echo "Unhealthy services: ${unhealthyServices.join(', ')} - deployment needed"
            return true

        case 'healthy':
            echo "All infrastructure services are healthy - no deployment needed"
            return false

        default:
            echo "Unknown status '${status}' - deployment needed for safety"
            return true
    }
}

// ========== DEPLOYMENT FUNCTIONS ==========

def deployInfrastructure() {
    echo "Triggering infrastructure deployment..."

    try {
        build job: env.INFRASTRUCTURE_JOB,
              parameters: [
                  string(name: 'DEPLOYMENT_MODE', value: 'deploy'),
                  booleanParam(name: 'FORCE_REDEPLOY', value: false)
              ],
              wait: true,
              propagate: true

        return [success: true]
    } catch (Exception e) {
        return [success: false, error: e.getMessage()]
    }
}

def validateProjectStructure() {
    echo "Validating project structure..."

    def requiredFiles = ['docker-compose.yml', 'Dockerfile']
    def missingFiles = requiredFiles.findAll { !fileExists(it) }

    if (missingFiles) {
        error "Missing required files: ${missingFiles.join(', ')}"
    }

    // Check for build configuration
    if (!fileExists('pom.xml') && !fileExists('build.gradle')) {
        error "No build configuration found (pom.xml or build.gradle)"
    }

    echo "✅ Project structure validated"
}

def buildApplication() {
    echo "Building application..."

    if (fileExists('pom.xml')) {
        sh 'mvn clean package -DskipTests'
    } else if (fileExists('build.gradle')) {
        sh './gradlew build -x test'
    }

    echo "✅ Application built successfully"
}

def buildDockerImage() {
    echo "Building Docker image..."
    sh 'docker compose build --no-cache'
    echo "✅ Docker image built successfully"
}

def deployMicroservice() {
    echo "Deploying microservice..."
    sh '''
        docker compose down || true
        docker compose up -d
    '''
    echo "✅ Microservice deployment initiated"
}

def verifyMicroserviceHealth() {
    echo "Verifying microservice health..."

    timeout(time: 120, unit: 'SECONDS') {
        waitUntil {
            try {
                // Microservice port IS exposed (8081), so we can check it normally
                sh "curl -f http://localhost:${env.MICROSERVICE_PORT}/actuator/health"
                return true
            } catch (Exception e) {
                echo "Waiting for microservice health..."
                sleep 5
                return false
            }
        }
    }

    echo "✅ Microservice is healthy and responding"
}