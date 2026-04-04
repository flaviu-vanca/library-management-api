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
        MAVEN_TOOL = 'Maven3'
        JDK25_TOOL = 'JDK25'
        JDK21_TOOL = 'JDK21'
    }

    stages {

        stage('Preflight Validation') {
            steps {
                script {
                    def missingTools = []
                    def requiredTools = [
                        [label: 'JDK 25', name: env.JDK25_TOOL],
                        [label: 'JDK 21', name: env.JDK21_TOOL],
                        [label: 'Maven', name: env.MAVEN_TOOL]
                    ]

                    requiredTools.each { t ->
                        try {
                            def resolved = tool(t.name)
                            echo "${t.label} tool '${t.name}' resolved at: ${resolved}"
                        } catch (Exception ex) {
                            echo "Failed to resolve ${t.label} tool '${t.name}': ${ex.message}"
                            missingTools << "${t.label} -> '${t.name}'"
                        }
                    }

                    if (missingTools) {
                        error("""Pipeline preflight failed: required Jenkins tools are missing.
Configure these in Manage Jenkins > Tools:
- ${missingTools.join('\n- ')}""")
                    }

                    try {
                        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'PRECHECK_SONAR_TOKEN')]) {
                            echo "Credential 'sonarqube-token' is configured."
                        }
                    } catch (Exception ex) {
                        error("""Pipeline preflight failed: credential 'sonarqube-token' was not found.
Create it as Secret text in Manage Jenkins > Credentials.
Details: ${ex.message}""")
                    }

                    try {
                        def _dockerRef = docker
                        echo "Docker Pipeline DSL is available."
                    } catch (MissingPropertyException ex) {
                        error("""Pipeline preflight failed: Docker Pipeline support is unavailable.
Install/enable the Docker Pipeline plugin (global variable: docker).""")
                    }

                    try {
                        httpRequest(
                            url: 'http://127.0.0.1:9/',
                            httpMode: 'HEAD',
                            timeout: 1,
                            validResponseCodes: '100:599',
                            quiet: true
                        )
                        echo "HTTP Request step is available."
                    } catch (NoSuchMethodError ex) {
                        error("""Pipeline preflight failed: httpRequest step is unavailable.
Install/enable the HTTP Request plugin.""")
                    } catch (MissingMethodException ex) {
                        error("""Pipeline preflight failed: httpRequest step is unavailable.
Install/enable the HTTP Request plugin.""")
                    } catch (Exception ex) {
                        echo "HTTP Request step is available."
                    }
                }
            }
        }

        stage('Verify Tooling') {
            tools {
                jdk "${JDK25_TOOL}"
                maven "${MAVEN_TOOL}"
            }
            steps {
                script {
                    echo "Resolved JDK 25 at: ${tool(env.JDK25_TOOL)}"
                    echo "Resolved Maven at: ${tool(env.MAVEN_TOOL)}"
                }
            }
        }

        stage('Build and Test - Java 25') {
            tools {
                jdk "${JDK25_TOOL}"
                maven "${MAVEN_TOOL}"
            }
            steps {
                step([
                    $class: 'hudson.tasks.Maven',
                    mavenName: "${env.MAVEN_TOOL}",
                    targets: '-B -ntp clean verify',
                    usePrivateRepository: true
                ])
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/karate-reports/**'
                }
            }
        }

        stage('Coverage and Static Analysis - Java 21') {
            tools {
                jdk "${JDK21_TOOL}"
                maven "${MAVEN_TOOL}"
            }
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    step([
                        $class: 'hudson.tasks.Maven',
                        mavenName: "${env.MAVEN_TOOL}",
                        targets: "-B -ntp -Pcoverage-java21 clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.host.url=${env.SONAR_URL} -Dsonar.token=${env.SONAR_TOKEN} -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} -Dsonar.projectName=\"Library Management API\" -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml",
                        usePrivateRepository: true
                    ])
                }
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('Package Artifact - Java 25') {
            tools {
                jdk "${JDK25_TOOL}"
                maven "${MAVEN_TOOL}"
            }
            steps {
                step([
                    $class: 'hudson.tasks.Maven',
                    mavenName: "${env.MAVEN_TOOL}",
                    targets: '-B -ntp -DskipTests clean package',
                    usePrivateRepository: true
                ])
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def image = docker.build("${env.APP_IMAGE}:${env.BUILD_NUMBER}")
                    image.tag('latest')
                }
            }
        }

        stage('Deploy Locally') {
            when {
                branch 'master'
            }
            steps {
                script {
                    docker.image("${env.APP_IMAGE}:${env.BUILD_NUMBER}").run("-p ${env.APP_PORT}:8080")

                    retry(20) {
                        sleep time: 3, unit: 'SECONDS'
                        def response = httpRequest(
                            url: "http://localhost:${env.APP_PORT}/",
                            validResponseCodes: '200',
                            quiet: true
                        )
                        echo response.content
                    }
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }

}
