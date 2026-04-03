$ErrorActionPreference = "Stop"
. "$PSScriptRoot\ci-common.ps1"

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
