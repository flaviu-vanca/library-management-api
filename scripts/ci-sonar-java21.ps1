param(
    [string]$SonarUrl = "http://localhost:9000",
    [string]$ProjectKey = "library-management-api",
    [string]$SonarAdminUser = "admin",
    [string]$SonarAdminPassword = "admin"
)

$ErrorActionPreference = "Stop"
. "$PSScriptRoot\ci-common.ps1"

$projectRoot = Resolve-ProjectRoot
$javaHome = Resolve-JavaHomeForMajorVersion -MajorVersion 21
$mavenRepo = Resolve-MavenRepo -ProjectRoot $projectRoot
$headers = New-BasicAuthHeader -Username $SonarAdminUser -Password $SonarAdminPassword
$tokenName = "jenkins-$([guid]::NewGuid().ToString('N'))"
$sonarToken = $null

Set-Location $projectRoot
Use-JavaHome -JavaHome $javaHome

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Using Maven repo=$mavenRepo"
& java -version

for ($attempt = 1; $attempt -le 30; $attempt++) {
    try {
        $status = (Invoke-RestMethod -Uri "$SonarUrl/api/system/status" -Headers $headers -Method Get).status
        if ($status -eq "UP") {
            break
        }
    }
    catch {
    }

    if ($attempt -eq 30) {
        throw "SonarQube at $SonarUrl did not become ready."
    }

    Write-Host "Waiting for SonarQube to become ready..."
    Start-Sleep -Seconds 10
}

try {
    $tokenResponse = Invoke-RestMethod -Uri "$SonarUrl/api/user_tokens/generate?name=$tokenName" -Headers $headers -Method Post
    $sonarToken = $tokenResponse.token

    Invoke-MavenCommand -Arguments @(
        "-Dmaven.repo.local=$mavenRepo",
        "-B",
        "-ntp",
        "-Pcoverage-java21",
        "verify",
        "org.sonarsource.scanner.maven:sonar-maven-plugin:sonar",
        "-Dsonar.host.url=$SonarUrl",
        "-Dsonar.token=$sonarToken",
        "-Dsonar.projectKey=$ProjectKey",
        "-Dsonar.projectName=Library Management API",
        "-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"
    )

    for ($attempt = 1; $attempt -le 30; $attempt++) {
        $quality = Invoke-RestMethod -Uri "$SonarUrl/api/qualitygates/project_status?projectKey=$ProjectKey" -Headers @{ Authorization = "Basic " + [Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${sonarToken}:")) } -Method Get
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
}
finally {
    if ($sonarToken) {
        try {
            Invoke-RestMethod -Uri "$SonarUrl/api/user_tokens/revoke?name=$tokenName" -Headers $headers -Method Post | Out-Null
        }
        catch {
            Write-Warning "Failed to revoke temporary SonarQube token $tokenName."
        }
    }
}
