def runGeneratedPowerShell(String scriptName, String scriptBody) {
    def scriptDir = '.jenkins-ci'
    bat """@echo on
if not exist "${scriptDir}" mkdir "${scriptDir}"
"""

    def scriptPath = "${scriptDir}\\${scriptName}"
    writeFile file: scriptPath, text: scriptBody.stripIndent() + '\n'

    bat """@echo on
powershell -NoProfile -ExecutionPolicy Bypass -File "${scriptPath}"
"""
}

def ciCommonPowerShell = '''
$ErrorActionPreference = "Stop"

function Get-UserProfileDirectories {
    $profilesRoot = "C:\\Users"
    if (-not (Test-Path $profilesRoot)) {
        return @()
    }

    return Get-ChildItem -Path $profilesRoot -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notin @("All Users", "Default", "Default User", "Public") } |
        ForEach-Object { $_.FullName }
}

function Test-JavaHomeMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaHome,
        [Parameter(Mandatory = $true)]
        [int]$MajorVersion
    )

    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return $false
    }

    $javaExe = Join-Path $JavaHome "bin\\java.exe"
    if (-not (Test-Path $javaExe)) {
        return $false
    }

    try {
        $versionOutput = & $javaExe -version 2>&1 | Out-String
        return [regex]::IsMatch($versionOutput, 'version "' + $MajorVersion + '(\.|")')
    }
    catch {
        return $false
    }
}

function Resolve-JavaHomeForMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [int]$MajorVersion
    )

    $candidateHomes = @()

    if ($env:JAVA_HOME) {
        $candidateHomes += $env:JAVA_HOME
    }

    $candidateRoots = @(
        (Join-Path $HOME ".sdkman\\candidates\\java"),
        (Join-Path $env:USERPROFILE ".sdkman\\candidates\\java"),
        "C:\\Program Files\\Java",
        "C:\\Program Files\\Eclipse Adoptium",
        "C:\\Program Files\\Microsoft"
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique

    foreach ($profileDir in Get-UserProfileDirectories) {
        $candidateRoots += Join-Path $profileDir ".sdkman\\candidates\\java"
    }

    $candidateRoots = $candidateRoots | Select-Object -Unique

    foreach ($root in $candidateRoots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $candidateHomes += Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { $_.FullName }
    }

    foreach ($candidateHome in ($candidateHomes | Select-Object -Unique)) {
        if (Test-JavaHomeMajorVersion -JavaHome $candidateHome -MajorVersion $MajorVersion) {
            return (Resolve-Path $candidateHome).Path
        }
    }

    throw "Java $MajorVersion was not found on this machine."
}

function Use-JavaHome {
    param(
        [Parameter(Mandatory = $true)]
        [string]$JavaHome
    )

    $env:JAVA_HOME = $JavaHome
    $env:PATH = "$env:JAVA_HOME\\bin;$env:PATH"
}

function Resolve-ProjectRoot {
    if ($env:WORKSPACE -and (Test-Path $env:WORKSPACE)) {
        return (Resolve-Path $env:WORKSPACE).Path
    }

    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Resolve-MavenRepo {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot
    )

    $repo = Join-Path $ProjectRoot ".m2\\repository"
    New-Item -ItemType Directory -Force -Path $repo | Out-Null
    return $repo
}

function Resolve-MavenExecutable {
    $candidates = @()

    if ($env:MAVEN_HOME) {
        $candidates += Join-Path $env:MAVEN_HOME "bin\\mvn.cmd"
    }

    $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mavenCommand -and $mavenCommand.Source) {
        $candidates += $mavenCommand.Source
    }

    $candidateRoots = @(
        (Join-Path $HOME ".sdkman\\candidates\\maven"),
        (Join-Path $env:USERPROFILE ".sdkman\\candidates\\maven"),
        "C:\\Program Files\\Apache\\maven",
        "C:\\Program Files\\Apache Maven",
        "C:\\ProgramData\\chocolatey\\lib\\maven\\apache-maven-*"
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }

    foreach ($profileDir in Get-UserProfileDirectories) {
        $candidateRoots += Join-Path $profileDir ".sdkman\\candidates\\maven"
    }

    foreach ($root in ($candidateRoots | Select-Object -Unique)) {
        if ($root -match '\*') {
            $resolvedRoots = Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
        }
        elseif (Test-Path $root) {
            $resolvedRoots = @($root)
        }
        else {
            $resolvedRoots = @()
        }

        foreach ($resolvedRoot in $resolvedRoots) {
            if (-not (Test-Path $resolvedRoot)) {
                continue
            }

            $candidates += Get-ChildItem -Path $resolvedRoot -Directory -ErrorAction SilentlyContinue |
                ForEach-Object { Join-Path $_.FullName "bin\\mvn.cmd" }
            $candidates += Join-Path $resolvedRoot "current\\bin\\mvn.cmd"
            $candidates += Join-Path $resolvedRoot "bin\\mvn.cmd"
        }
    }

    foreach ($candidate in ($candidates | Select-Object -Unique)) {
        if (Test-Path $candidate) {
            return (Resolve-Path $candidate).Path
        }
    }

    throw "Maven was not found on this machine."
}

function Invoke-MavenCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $mavenExe = Resolve-MavenExecutable
    & $mavenExe @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Maven failed with exit code $LASTEXITCODE"
    }
}
'''

def buildJava25PowerShell = ciCommonPowerShell + '''
$projectRoot = Resolve-ProjectRoot
$javaHome = Resolve-JavaHomeForMajorVersion -MajorVersion 25
$mavenRepo = Resolve-MavenRepo -ProjectRoot $projectRoot

Set-Location $projectRoot
Use-JavaHome -JavaHome $javaHome

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Using Maven repo=$mavenRepo"
& java -version

Invoke-MavenCommand -Arguments @(
    "-Dmaven.repo.local=$mavenRepo",
    "-B",
    "-ntp",
    "clean",
    "verify"
)
'''

def sonarJava21PowerShell = ciCommonPowerShell + '''
$projectRoot = Resolve-ProjectRoot
$javaHome = Resolve-JavaHomeForMajorVersion -MajorVersion 21
$mavenRepo = Resolve-MavenRepo -ProjectRoot $projectRoot
$analysisToken = $env:SONAR_TOKEN

if ([string]::IsNullOrWhiteSpace($analysisToken)) {
    throw "SONAR_TOKEN was not provided."
}

$sonarUrl = $env:SONAR_URL
$projectKey = $env:SONAR_PROJECT_KEY
$headers = @{
    Authorization = "Basic " + [Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${analysisToken}:"))
}

Set-Location $projectRoot
Use-JavaHome -JavaHome $javaHome

$targetDir = Join-Path $projectRoot "target"
if (Test-Path $targetDir) {
    Write-Host "Removing stale build output from $targetDir"
    Remove-Item -Recurse -Force $targetDir
}

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Using Maven repo=$mavenRepo"
& java -version

for ($attempt = 1; $attempt -le 30; $attempt++) {
    try {
        $status = (Invoke-RestMethod -Uri "$sonarUrl/api/system/status" -Headers $headers -Method Get).status
        if ($status -eq "UP") {
            break
        }
    }
    catch {
    }

    if ($attempt -eq 30) {
        throw "SonarQube at $sonarUrl did not become ready."
    }

    Write-Host "Waiting for SonarQube to become ready..."
    Start-Sleep -Seconds 10
}

Invoke-MavenCommand -Arguments @(
    "-Dmaven.repo.local=$mavenRepo",
    "-B",
    "-ntp",
    "-Pcoverage-java21",
    "clean",
    "verify",
    "org.sonarsource.scanner.maven:sonar-maven-plugin:sonar",
    "-Dsonar.host.url=$sonarUrl",
    "-Dsonar.token=$analysisToken",
    "-Dsonar.projectKey=$projectKey",
    "-Dsonar.projectName=Library Management API",
    "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
)

for ($attempt = 1; $attempt -le 30; $attempt++) {
    $quality = Invoke-RestMethod -Uri "$sonarUrl/api/qualitygates/project_status?projectKey=$projectKey" -Headers $headers -Method Get
    $qualityStatus = $quality.projectStatus.status

    if ($qualityStatus -ne "PENDING" -and $qualityStatus -ne "NONE") {
        if ($qualityStatus -ne "OK") {
            throw "SonarQube quality gate failed with status: $qualityStatus"
        }
        break
    }

    if ($attempt -eq 30) {
        throw "Timed out waiting for SonarQube quality gate."
    }

    Write-Host "Waiting for SonarQube quality gate..."
    Start-Sleep -Seconds 10
}
'''

def packageJava25PowerShell = ciCommonPowerShell + '''
$projectRoot = Resolve-ProjectRoot
$javaHome = Resolve-JavaHomeForMajorVersion -MajorVersion 25
$mavenRepo = Resolve-MavenRepo -ProjectRoot $projectRoot

Set-Location $projectRoot
Use-JavaHome -JavaHome $javaHome

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Using Maven repo=$mavenRepo"
& java -version

Invoke-MavenCommand -Arguments @(
    "-Dmaven.repo.local=$mavenRepo",
    "-B",
    "-ntp",
    "-DskipTests",
    "clean",
    "package"
)
'''

def deployLocalPowerShell = '''
$ErrorActionPreference = "Stop"

$AppContainer = $env:APP_CONTAINER
$AppImage = $env:APP_IMAGE
$Tag = $env:BUILD_NUMBER
$AppPort = [int]$env:APP_PORT

docker rm -f $AppContainer 2>$null | Out-Null

& docker run -d --name $AppContainer -p "${AppPort}:8080" "${AppImage}:${Tag}"
if ($LASTEXITCODE -ne 0) {
    throw "docker run failed with exit code $LASTEXITCODE"
}

for ($attempt = 1; $attempt -le 20; $attempt++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:${AppPort}/" -UseBasicParsing
        if ($response.StatusCode -eq 200) {
            Write-Host $response.Content
            exit 0
        }
    }
    catch {
    }

    Start-Sleep -Seconds 3
}

throw "Deployed container did not become healthy on http://localhost:${AppPort}/"
'''

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
                script {
                    runGeneratedPowerShell('ci-build-java25.ps1', buildJava25PowerShell)
                }
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
                    script {
                        runGeneratedPowerShell('ci-sonar-java21.ps1', sonarJava21PowerShell)
                    }
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
                script {
                    runGeneratedPowerShell('ci-package-java25.ps1', packageJava25PowerShell)
                }
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
                branch 'main'
            }
            steps {
                script {
                    runGeneratedPowerShell('ci-deploy-local.ps1', deployLocalPowerShell)
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
