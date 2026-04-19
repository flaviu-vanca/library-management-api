$ErrorActionPreference = "Stop"
if ($null -ne (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue)) {
    $PSNativeCommandUseErrorActionPreference = $false
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$reportPath = Join-Path $projectRoot "target\site\jacoco\index.html"
$logPath = Join-Path $projectRoot "coverage-test-run.log"

function Test-JavaHomeMajorVersion {
    param(
        [string]$JavaHome,
        [int]$MajorVersion
    )

    if ([string]::IsNullOrWhiteSpace($JavaHome)) {
        return $false
    }

    $javaExe = Join-Path $JavaHome "bin\java.exe"
    if (-not (Test-Path $javaExe)) {
        return $false
    }

    $releaseFile = Join-Path $JavaHome "release"
    if (Test-Path $releaseFile) {
        $versionLine = Get-Content $releaseFile -ErrorAction SilentlyContinue |
            Where-Object { $_ -match '^JAVA_VERSION=' } |
            Select-Object -First 1

        if ($versionLine) {
            $version = ($versionLine -replace '^JAVA_VERSION="?','' -replace '"$','')
            if ($version -match "^$MajorVersion(\.|$)") {
                return $true
            }
        }
    }

    try {
        $versionOutput = & $javaExe -version 2>&1 | Out-String
        return [regex]::IsMatch($versionOutput, 'version "' + $MajorVersion + '(\.|")')
    }
    catch {
        return $false
    }
}

function Resolve-Java21Home {
    $candidateHomes = @()

    if ($env:JAVA_HOME) {
        $candidateHomes += $env:JAVA_HOME
    }

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand -and $javaCommand.Source) {
        $javaBin = Split-Path -Parent $javaCommand.Source
        $candidateHomes += Split-Path -Parent $javaBin
    }

    $candidateRoots = @(
        (Join-Path $HOME ".sdkman\candidates\java"),
        (Join-Path $env:USERPROFILE ".sdkman\candidates\java"),
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft"
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique

    foreach ($root in $candidateRoots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $candidateHomes += Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { $_.FullName }
    }

    foreach ($candidateHome in ($candidateHomes | Select-Object -Unique)) {
        if (Test-JavaHomeMajorVersion -JavaHome $candidateHome -MajorVersion 21) {
            return (Resolve-Path $candidateHome).Path
        }
    }

    return $null
}

function Resolve-MavenRepo {
    $candidates = @(
        (Join-Path $HOME ".m2\repository"),
        (Join-Path $env:USERPROFILE ".m2\repository"),
        (Join-Path $projectRoot ".m2\repository")
    ) | Select-Object -Unique

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
        throw "Java 21 was not found. Install JDK 21 or set JAVA_HOME to a Java 21 installation before running this script."
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

