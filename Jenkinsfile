pipeline {

    agent any

    environment {
        DOCKER_IMAGE = "umanikanta/order-service"
        IMAGE_TAG = "develop-${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source code'
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

                // SonarQube command goes here
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                sh """
                    docker build \
                    -t ${DOCKER_IMAGE}:${IMAGE_TAG} .
                """
            }
        }

        stage('Security Scan') {
            steps {
                echo "Running security scan: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                // Example:
                // sh "trivy image ${DOCKER_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Docker Push') {

            when {
                branch 'develop'
            }

            steps {

                echo "Pushing Docker image: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login \
                            -u "$DOCKER_USERNAME" \
                            --password-stdin

                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to DEV') {

            when {
                branch 'develop'
            }

            steps {

                echo "Deploying ${DOCKER_IMAGE}:${IMAGE_TAG} to DEV"

                sh """
                    kubectl -n development set image deployment/order-service \
                        order-service=${DOCKER_IMAGE}:${IMAGE_TAG}
                """

                sh """
                    kubectl -n development rollout status \
                        deployment/order-service \
                        --timeout=120s
                """
            }
        }

        stage('Smoke Test') {

            when {
                branch 'develop'
            }

            steps {

                echo 'Running DEV smoke test'

                sh '''
                    kubectl get pods -n development
                    kubectl get svc -n development
                '''
            }
        }
    }

    post {

        success {
            echo "CI/CD pipeline SUCCESS - Branch: ${BRANCH_NAME}"
        }

        failure {
            echo "CI/CD pipeline FAILED - Branch: ${BRANCH_NAME}"
        }
    }
}