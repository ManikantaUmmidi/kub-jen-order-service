pipeline {

    agent any

    environment {

        IMAGE_NAME = "order-service"

        IMAGE_TAG = "feature-${BUILD_NUMBER}"

    }

    stages {

        stage('Checkout') {

            steps {

                checkout scm

            }
        }

        stage('Build') {

            steps {

                echo 'Building application'

                sh 'mvn clean package -DskipTests'

            }
        }

        stage('Unit Tests') {

            steps {

                echo 'Running unit tests'

                sh 'mvn test'

            }
        }

        stage('Code Quality') {

            steps {

                echo 'Running code quality analysis'

                // SonarQube command would go here
            }
        }

        stage('Docker Build') {

            steps {

                echo 'Building Docker image'

                sh """
                    docker build \
                    -t ${IMAGE_NAME}:${IMAGE_TAG} .
                """
            }
        }

        stage('Security Scan') {

            steps {

                echo 'Running security scan'

                // Example:
                // trivy image ${IMAGE_NAME}:${IMAGE_TAG}
            }
        }

    }

    post {

        success {

            echo 'Feature branch CI pipeline SUCCESS'

        }

        failure {

            echo 'Feature branch CI pipeline FAILED'

        }
    }
}