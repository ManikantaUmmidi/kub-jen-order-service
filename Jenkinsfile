pipeline {

    agent any

    environment {

        IMAGE_NAME = "order-service"

        IMAGE_TAG = "develop-${BUILD_NUMBER}"

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

        stage('Docker Push') {
                    steps {
                        echo "Pushing Docker image"

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

                                docker push ${IMAGE_NAME}:${IMAGE_TAG}

                                docker logout
                            '''
                        }
                    }
                }

                stage('Deploy to DEV') {

                    steps {

                        sh '''
                            kubectl -n development set image deployment/order-service \
                              order-service=${IMAGE_NAME}:${IMAGE_TAG}
                        '''

                        sh '''
                            kubectl -n development rollout status \
                              deployment/order-service \
                              --timeout=120s
                        '''
                    }
                }

                stage('Smoke Test') {

                    steps {

                        sh '''
                            kubectl get pods -n development
                            kubectl get svc -n development
                        '''
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