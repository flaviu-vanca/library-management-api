$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$reportPath = Join-Path $projectRoot "target\site\jacoco\index.html"
$logPath = Join-Path $projectRoot "coverage-test-run.log"

function Resolve-Java21Home {
    $candidates = @(
        "C:\Users\skety\.sdkman\candidates\java\21.0.9-oracle",
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "C:\Program Files\Microsoft\jdk-21*"
    )

    foreach ($candidate in $candidates) {
        if ($candidate -notmatch '[*?]' -and (Test-Path $candidate)) {
            return (Resolve-Path $candidate).Path
        }

        $matches = Get-ChildItem -Path $candidate -Directory -ErrorAction SilentlyContinue
        foreach ($match in $matches) {
            if (Test-Path (Join-Path $match.FullName "bin\java.exe")) {
                return $match.FullName
            }
        }
    }

    return $null
}

function Resolve-MavenRepo {
    $candidates = @(
        (Join-Path $HOME ".m2\repository"),
        "C:\Users\skety\.m2\repository",
        (Join-Path $projectRoot ".m2\repository")
    )

    foreach ($candidate in $candidates) {
        try {
            New-Item -ItemType Directory -Force -Path $candidate -ErrorAction Stop | Out-Null
            return $candidate
        }
        catch {
        }
    }

    throw "No writable Maven repository location was found."
}

try {
    Set-Location $projectRoot

    $java21 = Resolve-Java21Home
    if (-not $java21) {
        throw "Java 21 was not found. Install JDK 21 or update scripts\run-screencast-demo.ps1 with the correct path."
    }
    $mavenRepo = Resolve-MavenRepo

    $env:JAVA_HOME = $java21
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

    Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
    Write-Host "Using Maven repo=$mavenRepo"
    & cmd /c "java -version 2>&1"
    if ($LASTEXITCODE -ne 0) {
        throw "java -version failed with exit code $LASTEXITCODE"
    }

    Write-Host ""
    Write-Host "Running clean coverage test suite..."
    Write-Host "Log file: $logPath"
    Write-Host ""

    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $logPath) | Out-Null
    if (Test-Path $logPath) {
        Remove-Item $logPath -Force
    }
    & cmd /c "mvn ""-Dmaven.repo.local=$mavenRepo"" clean -Pcoverage-java21 test 2>&1" | Tee-Object -FilePath $logPath
    if ($LASTEXITCODE -ne 0) {
        throw "Maven failed with exit code $LASTEXITCODE. See $logPath"
    }

    if (-not (Test-Path $reportPath)) {
        throw "Coverage run finished, but the report was not found at $reportPath"
    }

    Write-Host ""
    Write-Host "Coverage report:"
    Write-Host $reportPath
    Write-Host ""
    Write-Host "Screencast demo completed successfully."
}
catch {
    Write-Host ""
    Write-Host "Screencast demo failed." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if (Test-Path $logPath) {
        Write-Host "Check log: $logPath" -ForegroundColor Yellow
    }
}
finally {
    Write-Host ""
    Read-Host "Press Enter to close"
}

