pipeline {

    agent any

    /*
     * Common environment variables
     *
     * IMAGE_TAG is intentionally NOT declared here.
     * It is calculated dynamically in the Initialize stage.
     */
    environment {

        DOCKER_IMAGE = "umanikanta/order-service"
    }

    stages {

        // ============================================================
        // 1. CHECKOUT
        // ============================================================

        stage('Checkout') {

            steps {

                echo "========================================"
                echo "Checking out source code"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "========================================"

                checkout scm
            }
        }


        // ============================================================
        // 2. INITIALIZE
        // ============================================================

        stage('Initialize') {

            steps {

                script {

                    /*
                     * Generate Docker image tag based on branch.
                     *
                     * feature/cancel-order
                     *      ->
                     * feature-10
                     *
                     * develop
                     *      ->
                     * develop-20
                     *
                     * main
                     *      ->
                     * release-30
                     */

                    if (env.BRANCH_NAME.startsWith('feature/')) {

                        env.IMAGE_TAG =
                            "feature-${env.BUILD_NUMBER}"

                    }

                    else if (env.BRANCH_NAME == 'develop') {

                        env.IMAGE_TAG =
                            "develop-${env.BUILD_NUMBER}"

                    }

                    else if (env.BRANCH_NAME == 'main') {

                        env.IMAGE_TAG =
                            "release-${env.BUILD_NUMBER}"

                    }

                    else {

                        error """
                        Unsupported branch: ${env.BRANCH_NAME}

                        Supported branches:
                        - feature/*
                        - develop
                        - main
                        """
                    }


                    echo ""
                    echo "========================================"
                    echo "PIPELINE INITIALIZATION"
                    echo "========================================"
                    echo "Branch     : ${env.BRANCH_NAME}"
                    echo "Build      : ${env.BUILD_NUMBER}"
                    echo "Image      : ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
                    echo "========================================"
                    echo ""
                }
            }
        }


        // ============================================================
        // 3. BUILD
        // ============================================================

        stage('Build') {

            steps {

                echo "========================================"
                echo "Building Spring Boot application"
                echo "========================================"

                sh 'mvn clean package -DskipTests'
            }
        }


        // ============================================================
        // 4. UNIT TESTS
        // ============================================================

        stage('Unit Tests') {

            steps {

                echo "========================================"
                echo "Running unit tests"
                echo "========================================"

                sh 'mvn test'
            }
        }


        // ============================================================
        // 5. CODE QUALITY
        // ============================================================

        stage('Code Quality') {

            steps {

                echo "========================================"
                echo "Running code quality analysis"
                echo "========================================"

                /*
                 * SonarQube command can be added here later.
                 *
                 * Example:
                 *
                 * sh 'mvn sonar:sonar'
                 */
            }
        }


        // ============================================================
        // 6. DOCKER BUILD
        // ============================================================

        stage('Docker Build') {

            steps {

                echo "========================================"
                echo "Building Docker image"
                echo "========================================"

                echo "Image: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"

                sh """
                    docker build \
                    -t ${env.DOCKER_IMAGE}:${env.IMAGE_TAG} .
                """
            }
        }


        // ============================================================
        // 7. SECURITY SCAN
        // ============================================================

        stage('Security Scan') {

            steps {

                echo "========================================"
                echo "Running security scan"
                echo "========================================"

                echo "Scanning:"
                echo "${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"

                /*
                 * Trivy can be added later.
                 *
                 * Example:
                 *
                 * sh "trivy image ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
                 */
            }
        }


        // ============================================================
        // 8. DOCKER PUSH
        // ============================================================

        /*
         * FEATURE BRANCH:
         *
         * Docker image is built but NOT pushed.
         *
         * DEVELOP:
         *
         * Docker image is pushed.
         *
         * MAIN:
         *
         * Docker image is pushed.
         */

        stage('Docker Push') {

            when {

                anyOf {

                    branch 'develop'

                    branch 'main'
                }
            }

            steps {

                echo "========================================"
                echo "Publishing Docker image"
                echo "========================================"

                echo "Image:"
                echo "${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"


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


        // ============================================================
        // 9. DEPLOY TO DEVELOPMENT
        // ============================================================

        /*
         * Only develop branch reaches this stage.
         */

        stage('Deploy to DEV') {

            when {

                branch 'develop'
            }

            steps {

                echo "========================================"
                echo "DEPLOYING TO DEVELOPMENT"
                echo "========================================"

                echo "Image:"
                echo "${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"


                sh """

                    kubectl -n development set image \
                        deployment/order-service \
                        order-service=${env.DOCKER_IMAGE}:${env.IMAGE_TAG}

                """


                echo "Waiting for DEV rollout..."


                sh """

                    kubectl -n development rollout status \
                        deployment/order-service \
                        --timeout=120s

                """
            }
        }


        // ============================================================
        // 10. DEV SMOKE TEST
        // ============================================================

        stage('DEV Smoke Test') {

            when {

                branch 'develop'
            }

            steps {

                echo "========================================"
                echo "RUNNING DEV SMOKE TEST"
                echo "========================================"


                sh '''

                    echo "Pods:"
                    kubectl get pods -n development

                    echo ""

                    echo "Services:"
                    kubectl get svc -n development

                    echo ""

                    echo "Deployment:"
                    kubectl get deployment order-service \
                        -n development

                '''
            }
        }


        // ============================================================
        // 11. PRODUCTION APPROVAL
        // ============================================================

        /*
         * Only main branch reaches this stage.
         *
         * Jenkins pauses here and waits for manual approval.
         */

        stage('Production Approval') {

            when {

                branch 'main'
            }

            steps {

                echo "========================================"
                echo "PRODUCTION DEPLOYMENT APPROVAL"
                echo "========================================"

                input(

                    message:
                        "Deploy ${env.DOCKER_IMAGE}:${env.IMAGE_TAG} to PRODUCTION?",

                    ok:
                        'Deploy to Production'
                )
            }
        }


        // ============================================================
        // 12. DEPLOY TO PRODUCTION
        // ============================================================

        /*
         * Only main branch reaches this stage.
         */

        stage('Deploy to PROD') {

            when {

                branch 'main'
            }

            steps {

                echo "========================================"
                echo "DEPLOYING TO PRODUCTION"
                echo "========================================"

                echo "Image:"
                echo "${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"


                sh """

                    kubectl -n production set image \
                        deployment/order-service \
                        order-service=${env.DOCKER_IMAGE}:${env.IMAGE_TAG}

                """


                echo "Waiting for production rollout..."


                sh """

                    kubectl -n production rollout status \
                        deployment/order-service \
                        --timeout=180s

                """
            }
        }


        // ============================================================
        // 13. PRODUCTION SMOKE TEST
        // ============================================================

        stage('Production Smoke Test') {

            when {

                branch 'main'
            }

            steps {

                echo "========================================"
                echo "RUNNING PRODUCTION SMOKE TEST"
                echo "========================================"


                sh '''

                    echo "Pods:"
                    kubectl get pods -n production

                    echo ""

                    echo "Services:"
                    kubectl get svc -n production

                    echo ""

                    echo "Deployment:"
                    kubectl get deployment order-service \
                        -n production

                '''
            }
        }
    }


    // ================================================================
    // POST ACTIONS
    // ================================================================

    post {

        success {

            echo ""
            echo "========================================"
            echo "CI/CD PIPELINE SUCCESS"
            echo "========================================"
            echo "Branch : ${env.BRANCH_NAME}"
            echo "Build  : ${env.BUILD_NUMBER}"
            echo "Image  : ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
            echo "========================================"
            echo ""
        }


        failure {

            echo ""
            echo "========================================"
            echo "CI/CD PIPELINE FAILED"
            echo "========================================"
            echo "Branch : ${env.BRANCH_NAME}"
            echo "Build  : ${env.BUILD_NUMBER}"
            echo "Image  : ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}"
            echo "========================================"
            echo ""
        }


        always {

            echo ""
            echo "Pipeline execution completed."
            echo "Branch: ${env.BRANCH_NAME}"
            echo ""
        }
    }
}