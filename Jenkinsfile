pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    triggers {
        githubPush()
    }

    environment {
        APP_CONTAINER = 'library-management-api'
        APP_IMAGE = 'library-management-api'
        APP_PORT = '8082'
        SONAR_URL = 'http://localhost:9000'
        SONAR_PROJECT_KEY = 'library-management-api'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Tooling') {
            steps {
                bat '''
                    @echo on
                    java -version
                    mvn -version
                    docker version
                '''
            }
        }

        stage('Build and Test - Java 25') {
            steps {
                bat '''
                    @echo on
                    powershell -NoProfile -ExecutionPolicy Bypass -File ".\\scripts\\ci-build-java25.ps1"
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/karate-reports/**'
                }
            }
        }

        stage('Coverage and Static Analysis - Java 21') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'sonarqube-admin', usernameVariable: 'SONAR_ADMIN_USER', passwordVariable: 'SONAR_ADMIN_PASSWORD')]) {
                    bat '''
                        @echo on
                        powershell -NoProfile -ExecutionPolicy Bypass -File ".\\scripts\\ci-sonar-java21.ps1" ^
                          -SonarUrl "%SONAR_URL%" ^
                          -ProjectKey "%SONAR_PROJECT_KEY%" ^
                          -SonarAdminUser "%SONAR_ADMIN_USER%" ^
                          -SonarAdminPassword "%SONAR_ADMIN_PASSWORD%"
                    '''
                }
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('Package Artifact - Java 25') {
            steps {
                bat '''
                    @echo on
                    powershell -NoProfile -ExecutionPolicy Bypass -File ".\\scripts\\ci-package-java25.ps1"
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    @echo on
                    docker build -t %APP_IMAGE%:%BUILD_NUMBER% -t %APP_IMAGE%:latest .
                '''
            }
        }

        stage('Deploy Locally') {
            when {
                branch 'master'
            }
            steps {
                bat '''
                    @echo on
                    powershell -NoProfile -ExecutionPolicy Bypass -File ".\\scripts\\ci-deploy-local.ps1" ^
                      -AppContainer "%APP_CONTAINER%" ^
                      -AppImage "%APP_IMAGE%" ^
                      -Tag "%BUILD_NUMBER%" ^
                      -AppPort %APP_PORT%
                '''
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}
