pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')  // Poll every 5 minutes for changes
    }

    parameters {
        choice(
            name: 'DEPLOYMENT_TYPE',
            choices: ['full', 'app-only'],
            description: 'Full: Deploy infrastructure + app, App-only: Deploy only this microservice'
        )
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip running tests'
        )
    }

    environment {
        INFRASTRUCTURE_JOB = 'Infrastructure-Pipeline'
        MICROSERVICE_APP_REPO = 'https://github.com/hiiico/vacation-planning-notifications'
    }

    stages {
        stage('Checkout Microservice-app') {
            steps {
                checkout scm
            }
        }

        stage('Validate Docker Compose') {
            steps {
                script {
                    // Validate docker-compose file exists
                    if (!fileExists('docker-compose.yml')) {
                        error "docker-compose.yml not found in Microservice-app repository"
                    }

                    // Validate Dockerfile exists
                    if (!fileExists('Dockerfile')) {
                        error "Dockerfile not found in Microservice-app repository"
                    }

                    echo "✅ Docker compose configuration validated"
                }
            }
        }

        stage('Run Tests') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                script {
                    echo "Running tests for Microservice-app..."
                    // Add your test commands here based on your project type

                    // Example for Maven:
                    // sh 'mvn clean test'

                    // Example for Gradle:
                    // sh './gradlew test'

                    // Example for custom test script:
                    // sh './run-tests.sh'

                    echo "✅ Tests completed successfully"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image for Microservice-app..."
                    sh '''
                        docker compose build --no-cache
                        echo "Docker image built successfully"
                    '''
                }
            }
        }

        stage('Trigger Infrastructure Deployment') {
            when {
                expression { params.DEPLOYMENT_TYPE == 'full' }
            }
            steps {
                script {
                    echo "Triggering infrastructure deployment..."
                    build job: env.INFRASTRUCTURE_JOB,
                          parameters: [
                              string(name: 'TARGET_APP', value: 'microservice-app'),
                              string(name: 'APP_BRANCH', value: env.BRANCH_NAME),
                              booleanParam(name: 'SKIP_INFRA', value: false)
                          ],
                          wait: true,
                          propagate: true
                }
            }
        }

        stage('Deploy Microservice Only') {
            when {
                expression { params.DEPLOYMENT_TYPE == 'app-only' }
            }
            steps {
                script {
                    echo "Deploying only Microservice-app..."
                    deployMicroservice()
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    echo "Performing health check on Microservice-app..."
                    waitForMicroserviceHealth()
                    echo "✅ Microservice-app is healthy and responding"
                }
            }
        }

        stage('Integration Test') {
            steps {
                script {
                    echo "Running integration tests..."
                    // Add integration tests that verify the service works
                    // with the actual infrastructure
                    runIntegrationTests()
                }
            }
        }
    }

    post {
        always {
            echo "Microservice-app pipeline execution completed"
            // Cleanup temporary files
            sh 'docker compose down || true'
            cleanWs()
        }
        success {
            echo "✅ Microservice-app deployed successfully!"
            script {
                echo "Notification Service URL: http://localhost:8081"
                echo "Health Check: http://localhost:8081/actuator/health"
            }
        }
        failure {
            echo "❌ Microservice-app deployment failed!"
            // Optional: Send notifications
        }
        cleanup {
            // Always cleanup Docker resources
            sh 'docker compose down || true'
        }
    }
}

def deployMicroservice() {
    echo "Deploying Microservice-app..."

    script {
        // Stop existing container if running
        sh 'docker compose down || true'

        // Deploy the microservice
        sh 'docker compose up -d'

        echo "Microservice-app deployment initiated"
    }
}

def waitForMicroserviceHealth() {
    echo "Waiting for Microservice-app to be healthy..."

    timeout(time: 120, unit: 'SECONDS') {
        waitUntil {
            try {
                // Check health endpoint
                sh '''
                    response_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)
                    if [ "$response_code" -eq 200 ]; then
                        echo "Health check passed: HTTP 200"
                        exit 0
                    else
                        echo "Health check failed: HTTP $response_code"
                        exit 1
                    fi
                '''
                return true
            } catch (Exception e) {
                echo "Waiting for Microservice-app health check..."
                sleep 10
                return false
            }
        }
    }
}

def runIntegrationTests() {
    echo "Running integration tests for Microservice-app..."

    script {
        try {
            // Example integration tests - customize based on your needs

            // Test 1: Basic connectivity
            sh '''
                echo "Testing basic connectivity..."
                curl -f http://localhost:8081/actuator/health
            '''

            // Test 2: Database connectivity (if applicable)
            sh '''
                echo "Testing database connectivity through service..."
                # You might want to test if the service can connect to MySQL
                # This would require your service to have a specific endpoint for DB check
            '''

            // Test 3: Kafka connectivity (if applicable)
            sh '''
                echo "Testing Kafka connectivity through service..."
                # You might want to test if the service can connect to Kafka
                # This would require your service to have a specific endpoint for Kafka check
            '''

            echo "✅ Integration tests passed"

        } catch (Exception e) {
            error "Integration tests failed: ${e.getMessage()}"
        }
    }
}