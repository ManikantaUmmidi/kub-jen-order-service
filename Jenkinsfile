pipeline {

    agent any

    environment {

        DOCKER_IMAGE = "umanikanta/order-service"

        // Tag will be calculated based on branch
        IMAGE_TAG = ""

    }

    stages {

        stage('Checkout') {

            steps {

                echo "Checking out branch: ${BRANCH_NAME}"

                checkout scm
            }
        }

        stage('Initialize') {

            steps {

                script {

                    if (env.BRANCH_NAME.startsWith('feature/')) {

                        env.IMAGE_TAG = "feature-${BUILD_NUMBER}"

                    } else if (env.BRANCH_NAME == 'develop') {

                        env.IMAGE_TAG = "develop-${BUILD_NUMBER}"

                    } else if (env.BRANCH_NAME == 'main') {

                        env.IMAGE_TAG = "release-${BUILD_NUMBER}"

                    } else {

                        error "Unsupported branch: ${env.BRANCH_NAME}"
                    }

                    echo "Branch     : ${env.BRANCH_NAME}"
                    echo "Image      : ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
                }
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

                echo "Scanning Docker image: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                // Example:
                // sh "trivy image ${DOCKER_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Docker Push') {

            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                }
            }

            steps {

                echo "Publishing Docker image: ${DOCKER_IMAGE}:${IMAGE_TAG}"

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

        stage('DEV Smoke Test') {

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

        stage('Production Approval') {

            when {
                branch 'main'
            }

            steps {

                input(
                    message: "Deploy ${DOCKER_IMAGE}:${IMAGE_TAG} to PRODUCTION?",
                    ok: 'Deploy to Production'
                )
            }
        }

        stage('Deploy to PROD') {

            when {
                branch 'main'
            }

            steps {

                echo "Deploying ${DOCKER_IMAGE}:${IMAGE_TAG} to PRODUCTION"

                sh """
                    kubectl -n production set image deployment/order-service \
                        order-service=${DOCKER_IMAGE}:${IMAGE_TAG}
                """

                sh """
                    kubectl -n production rollout status \
                        deployment/order-service \
                        --timeout=180s
                """
            }
        }

        stage('Production Smoke Test') {

            when {
                branch 'main'
            }

            steps {

                echo 'Running production smoke test'

                sh '''
                    kubectl get pods -n production
                    kubectl get svc -n production
                '''
            }
        }
    }

    post {

        success {

            echo """
            CI/CD SUCCESS

            Branch : ${BRANCH_NAME}
            Image  : ${DOCKER_IMAGE}:${IMAGE_TAG}
            """
        }

        failure {

            echo """
            CI/CD FAILED

            Branch : ${BRANCH_NAME}
            Image  : ${DOCKER_IMAGE}:${IMAGE_TAG}
            """
        }
    }
}