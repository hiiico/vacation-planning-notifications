pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        INFRASTRUCTURE_JOB = 'Infrastructure'
        MICROSERVICE_PORT = '8081'
        REQUIRED_INFRA_SERVICES = 'shared-mysql-db,kafka'
        DOCKER_IMAGE_NAME = 'hiiico/vacation_planning-urlaubsplanung-notification'
        DOCKER_IMAGE_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
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

        stage('Load Environment Configuration') {
            steps {
                script {
                    // Use the dotenv-file credential for database and Kafka config
                    withCredentials([file(credentialsId: 'dotenv-file', variable: 'ENV_FILE')]) {
                        sh '''
                            echo "Loading environment configuration from secured file..."
                            cp "$ENV_FILE" .env
                            chmod 644 .env
                            echo "✅ Environment configuration loaded successfully"

                            # Display non-sensitive info about the env file
                            echo "Environment variables loaded:"
                            grep -E "^(MYSQL_|DB_|KAFKA_|SPRING_MAIL_)" .env | while read line; do
                                key=$(echo "$line" | cut -d'=' -f1)
                                echo "  - $key"
                            done
                        '''
                    }
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

        stage('Build and Push Docker Image') {
            steps {
                script {
                    // Use dockerhub-creds for Docker Hub authentication
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )]) {
                        buildAndPushDockerImage()
                    }
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
                echo "Docker Image: ${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}"
            }
        }
        failure {
            echo "❌ Microservice deployment failed!"
        }
    }
}

// ========== INFRASTRUCTURE CHECK FUNCTIONS (NO PORT EXPOSURE) ==========

def checkInfrastructureStatus() {
    echo "Checking infrastructure status (no port exposure - internal checks only)..."

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

        // Check if services are healthy using INTERNAL container checks only
        def healthStatus = checkInfrastructureHealthInternal(requiredServices)
        if (!healthStatus.healthy) {
            return "running-but-unhealthy:${healthStatus.unhealthyServices.join(',')}"
        }

        return "healthy"

    } catch (Exception e) {
        echo "Error checking infrastructure status: ${e.getMessage()}"
        return "error"
    }
}

def checkInfrastructureHealthInternal(services) {
    def unhealthyServices = []

    services.each { service ->
        try {
            switch(service) {
                case 'shared-mysql-db':
                    // Check MySQL using INTERNAL container command only (no port exposure)
                    sh '''
                        # Method 1: Check if MySQL process is running internally
                        if docker exec shared-mysql-db ps aux | grep -q "[m]ysqld"; then
                            echo "MySQL process is running internally"
                        else
                            # Method 2: Check container health status
                            if docker inspect --format="{{.State.Health.Status}}" shared-mysql-db 2>/dev/null | grep -q "healthy"; then
                                echo "MySQL container health check passed"
                            else
                                exit 1
                            fi
                        fi
                    '''
                    break

                case 'kafka':
                    // Check Kafka using INTERNAL container checks only (no port exposure)
                    sh '''
                        # Method 1: Check if Kafka process is running internally
                        if docker exec kafka ps aux | grep -q "[k]afka"; then
                            echo "Kafka process is running internally"
                        else
                            # Method 2: Check container health status
                            if docker inspect --format="{{.State.Health.Status}}" kafka 2>/dev/null | grep -q "healthy"; then
                                echo "Kafka container health check passed"
                            else
                                # Method 3: Check container uptime (if running long enough, assume healthy)
                                container_uptime=$(docker inspect --format='{{.State.StartedAt}}' kafka 2>/dev/null | xargs -I {} date -d {} +%s)
                                current_time=$(date +%s)
                                if [ $((current_time - container_uptime)) -gt 120 ]; then
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

// ========== DOCKER HUB FUNCTIONS ==========

def buildAndPushDockerImage() {
    echo "Building and pushing Docker image to Docker Hub..."

    // Fix Dockerfile for build (remove init-wrapper.sh)
    fixDockerfileForBuild()

    // Login to Docker Hub
    sh """
        echo "Logging in to Docker Hub as \$DOCKERHUB_USERNAME..."
        echo \$DOCKERHUB_PASSWORD | docker login -u \$DOCKERHUB_USERNAME --password-stdin
    """

    // Build the image using the .env file for environment variables
    sh """
        docker compose build --no-cache
    """

    // Tag the image
    sh """
        docker tag ${env.DOCKER_IMAGE_NAME} ${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}
        docker tag ${env.DOCKER_IMAGE_NAME} ${env.DOCKER_IMAGE_NAME}:latest
    """

    // Push the image
    sh """
        echo "Pushing Docker image to Docker Hub..."
        docker push ${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}
        docker push ${env.DOCKER_IMAGE_NAME}:latest
    """

    // Logout from Docker Hub for security
    sh "docker logout"

    echo "✅ Docker image built and pushed successfully: ${env.DOCKER_IMAGE_NAME}:${env.DOCKER_IMAGE_TAG}"
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
                // Only microservice port is exposed (8081), so we can check it normally
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

// ========== DOCKERFILE FIX FUNCTION ==========

def fixDockerfileForBuild() {
    echo "Fixing Dockerfile for build (removing init-wrapper.sh reference)..."

    // Check if Dockerfile has the problematic line
    if (fileExists('Dockerfile')) {
        def dockerfileContent = readFile('Dockerfile')

        if (dockerfileContent.contains('init-wrapper.sh')) {
            echo "Removing init-wrapper.sh reference from Dockerfile..."

            // Remove the problematic lines
            def fixedContent = dockerfileContent.replaceAll('COPY init-wrapper\\.sh /docker-entrypoint-initdb\\.d/', '')
                                               .replaceAll('RUN chmod \\+x /docker-entrypoint-initdb\\.d/init-wrapper\\.sh', '')

            // Write the fixed content back
            writeFile file: 'Dockerfile', text: fixedContent
            echo "✅ Dockerfile fixed for build"
        } else {
            echo "✅ Dockerfile doesn't contain init-wrapper.sh reference"
        }
    }
}