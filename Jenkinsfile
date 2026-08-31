pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Backend Build') {
            steps {
                dir('backend') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }
    }

    post {
        success {
            echo 'Frontend and backend built successfully.'
        }
        failure {
            echo 'Build failed.'
        }
    }
}
