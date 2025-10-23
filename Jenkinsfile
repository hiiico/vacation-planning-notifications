pipeline {
    agent any

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        INFRASTRUCTURE_JOB = 'Infrastructure'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Deploy Infrastructure') {
            steps {
                script {
                    echo "Deploying infrastructure first..."
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

        stage('Build and Deploy Microservice') {
            steps {
                script {
                    echo "Building and deploying microservice..."
                    sh '''
                        # Build the application
                        if [ -f "pom.xml" ]; then
                            mvn clean package -DskipTests
                        elif [ -f "build.gradle" ]; then
                            ./gradlew build -x test
                        fi

                        # Build and deploy Docker image
                        docker compose build --no-cache
                        docker compose down || true
                        docker compose up -d
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    timeout(time: 60, unit: 'SECONDS') {
                        waitUntil {
                            try {
                                sh 'curl -f http://localhost:8081/actuator/health'
                                return true
                            } catch (Exception e) {
                                sleep 5
                                return false
                            }
                        }
                    }
                    echo "✅ Microservice deployed successfully"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "🚀 Microservice deployment completed!"
        }
    }
}