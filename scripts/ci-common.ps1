$ErrorActionPreference = "Stop"

function Get-UserProfileDirectories {
    $profilesRoot = "C:\Users"
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

function Resolve-JavaHomeForMajorVersion {
    param(
        [Parameter(Mandatory = $true)]
        [int]$MajorVersion
    )

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

    foreach ($profileDir in Get-UserProfileDirectories) {
        $candidateRoots += Join-Path $profileDir ".sdkman\candidates\java"
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
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
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

    $repo = Join-Path $ProjectRoot ".m2\repository"
    New-Item -ItemType Directory -Force -Path $repo | Out-Null
    return $repo
}

function Resolve-MavenExecutable {
    $candidates = @()

    if ($env:MAVEN_HOME) {
        $candidates += Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
    }

    $mavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mavenCommand -and $mavenCommand.Source) {
        $candidates += $mavenCommand.Source
    }

    $candidateRoots = @(
        (Join-Path $HOME ".sdkman\candidates\maven"),
        (Join-Path $env:USERPROFILE ".sdkman\candidates\maven"),
        "C:\Program Files\Apache\maven",
        "C:\Program Files\Apache Maven",
        "C:\ProgramData\chocolatey\lib\maven\apache-maven-*"
    ) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }

    foreach ($profileDir in Get-UserProfileDirectories) {
        $candidateRoots += Join-Path $profileDir ".sdkman\candidates\maven"
    }

    foreach ($root in ($candidateRoots | Select-Object -Unique)) {
        if ($root -like '*`**') {
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
                ForEach-Object { Join-Path $_.FullName "bin\mvn.cmd" }
            $candidates += Join-Path $resolvedRoot "current\bin\mvn.cmd"
            $candidates += Join-Path $resolvedRoot "bin\mvn.cmd"
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

function New-BasicAuthHeader {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Username,
        [Parameter(Mandatory = $true)]
        [string]$Password
    )

    $bytes = [System.Text.Encoding]::ASCII.GetBytes("${Username}:${Password}")
    return @{
        Authorization = "Basic " + [Convert]::ToBase64String($bytes)
    }
}
