pipeline {
    agent any

    tools {
        maven 'Maven'
        nodejs 'NodeJS'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        PORT = '8080'
    }

    stages {
        stage('Backend Test') {
            steps {
                echo 'Running Backend Unit and Integration Tests...'
                dir('backend') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Backend Build') {
            steps {
                echo 'Compiling and Packaging Backend Fat JAR...'
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('Frontend Install & Build') {
            steps {
                echo 'Installing Frontend dependencies and building production bundle...'
                dir('frontend') {
                    sh 'npm ci || npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Smoke & Health Test') {
            steps {
                echo 'Validating Backend health endpoint...'
                dir('backend') {
                    sh '''
                        java -jar target/tracker-backend.jar &
                        SERVER_PID=$!
                        sleep 3
                        curl -f http://localhost:8080/api/health || { kill $SERVER_PID; exit 1; }
                        kill $SERVER_PID
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '🎉 Full CI/CD Pipeline Succeeded: Frontend and Backend built and verified.'
        }
        failure {
            echo '❌ Pipeline Build Failed. Check logs for details.'
        }
        always {
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
