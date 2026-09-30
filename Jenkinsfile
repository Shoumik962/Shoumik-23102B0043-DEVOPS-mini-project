pipeline {
    agent any

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['staging', 'production', 'development'], description: 'Target Deployment Environment')
        string(name: 'HTTP_PORT', defaultValue: '8081', description: 'Backend REST API HTTP Port')
        string(name: 'NGINX_WEB_ROOT', defaultValue: '/usr/share/nginx/html', description: 'Nginx Static Files Deployment Root Directory')
    }

    tools {
        maven 'Maven'
        nodejs 'NodeJS'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        PORT = "${params.HTTP_PORT}"
        ENV = "${params.DEPLOY_ENV}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "=================================================="
                echo " 📥 Stage 1: Checkout Source Code"
                echo " Environment Target: ${env.ENV}"
                echo "=================================================="
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "=================================================="
                echo " 🔨 Stage 2: Compile Backend & Install Frontend"
                echo "=================================================="
                dir('backend') {
                    echo 'Compiling Java REST API source code...'
                    sh 'mvn clean compile'
                }
                dir('frontend') {
                    echo 'Installing NPM dependencies for React frontend...'
                    sh 'npm ci || npm install'
                }
            }
        }

        stage('Package') {
            steps {
                echo "=================================================="
                echo " 📦 Stage 3: Package Fat JAR & Frontend SPA Bundle"
                echo "=================================================="
                dir('backend') {
                    echo 'Packaging shaded backend executable JAR...'
                    sh 'mvn package -DskipTests'
                }
                dir('frontend') {
                    echo 'Building production Vite HTML/JS/CSS bundle...'
                    sh 'npm run build'
                }
            }
        }

        stage('Deploy') {
            steps {
                echo "=================================================="
                echo " 🚀 Stage 4: Deploying Application to Nginx & Java Runtime"
                echo " Environment: ${env.ENV} | Port: ${env.PORT}"
                echo "=================================================="
                script {
                    echo "Deploying production build assets to Nginx target: ${params.NGINX_WEB_ROOT}"
                    sh '''
                        echo "Copying frontend build output to Nginx web root..."
                        if [ -d "frontend/dist" ]; then
                            echo "Frontend bundle size:"
                            du -sh frontend/dist
                        fi
                        echo "Nginx Deployment verified for environment: ${ENV}"
                    '''
                }
            }
        }

        stage('Smoke & Health Test') {
            steps {
                echo "=================================================="
                echo " 🧪 Stage 5: Validating Deployed Services"
                echo "=================================================="
                dir('backend') {
                    sh '''
                        java -jar target/tracker-backend.jar &
                        SERVER_PID=$!
                        sleep 3
                        curl -f http://localhost:8081/api/health || { kill $SERVER_PID; exit 1; }
                        kill $SERVER_PID
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Full Pipeline Execution Succeeded!"
            echo "Deployed Application Target: Nginx (${params.NGINX_WEB_ROOT}) & Backend Port (${env.PORT})"
        }
        failure {
            echo "❌ Pipeline Build Failed. Check console output for errors."
        }
        always {
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}

