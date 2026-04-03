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
                powershell '''
                    $ErrorActionPreference = "Stop"
                    $repo = Join-Path $PWD ".m2\\repository"
                    New-Item -ItemType Directory -Force -Path $repo | Out-Null
                    & mvn "-Dmaven.repo.local=$repo" -B -ntp clean verify
                    if ($LASTEXITCODE -ne 0) { throw "Maven failed with exit code $LASTEXITCODE" }
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
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    powershell '''
                        $ErrorActionPreference = "Stop"

                        function Resolve-Java21Home {
                            $roots = @(
                                (Join-Path $env:USERPROFILE ".sdkman\\candidates\\java"),
                                "C:\\Program Files\\Java",
                                "C:\\Program Files\\Eclipse Adoptium",
                                "C:\\Program Files\\Microsoft"
                            ) | Where-Object { $_ -and (Test-Path $_) }

                            foreach ($root in $roots) {
                                $hit = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
                                    Where-Object { $_.Name -match '^21(\\.|$|\\D)' } |
                                    Select-Object -First 1
                                if ($hit) { return $hit.FullName }
                            }

                            throw "Java 21 was not found on this machine."
                        }

                        $java21 = Resolve-Java21Home
                        $env:JAVA_HOME = $java21
                        $env:PATH = "$env:JAVA_HOME\\bin;$env:PATH"

                        if (Test-Path "target") { Remove-Item -Recurse -Force "target" }

                        $repo = Join-Path $PWD ".m2\\repository"
                        New-Item -ItemType Directory -Force -Path $repo | Out-Null

                        & java -version
                        & mvn "-Dmaven.repo.local=$repo" -B -ntp -Pcoverage-java21 clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar "-Dsonar.host.url=$env:SONAR_URL" "-Dsonar.token=$env:SONAR_TOKEN" "-Dsonar.projectKey=$env:SONAR_PROJECT_KEY" "-Dsonar.projectName=Library Management API" "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
                        if ($LASTEXITCODE -ne 0) { throw "Maven failed with exit code $LASTEXITCODE" }
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
                powershell '''
                    $ErrorActionPreference = "Stop"
                    $repo = Join-Path $PWD ".m2\\repository"
                    New-Item -ItemType Directory -Force -Path $repo | Out-Null
                    & mvn "-Dmaven.repo.local=$repo" -B -ntp -DskipTests clean package
                    if ($LASTEXITCODE -ne 0) { throw "Maven failed with exit code $LASTEXITCODE" }
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
                powershell '''
                    $ErrorActionPreference = "Stop"

                    docker rm -f $env:APP_CONTAINER 2>$null | Out-Null
                    & docker run -d --name $env:APP_CONTAINER -p "$env:APP_PORT:8080" "$env:APP_IMAGE:$env:BUILD_NUMBER"
                    if ($LASTEXITCODE -ne 0) { throw "docker run failed with exit code $LASTEXITCODE" }

                    for ($attempt = 1; $attempt -le 20; $attempt++) {
                        try {
                            $response = Invoke-WebRequest -Uri "http://localhost:$env:APP_PORT/" -UseBasicParsing
                            if ($response.StatusCode -eq 200) {
                                Write-Host $response.Content
                                exit 0
                            }
                        }
                        catch {
                        }

                        Start-Sleep -Seconds 3
                    }

                    throw "Deployed container did not become healthy on http://localhost:$env:APP_PORT/"
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
