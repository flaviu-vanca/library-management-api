$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$paths = @(
    (Join-Path $projectRoot "target\karate-reports\karate-summary.html"),
    (Join-Path $projectRoot "target\site\jacoco\index.html")
)

$existing = $paths | Where-Object { Test-Path $_ }
$missing = $paths | Where-Object { -not (Test-Path $_) }

if (-not $existing) {
    Write-Host "No generated HTML reports were found." -ForegroundColor Red
    Write-Host "Run the tests and coverage first, then try again."
    exit 1
}

foreach ($path in $existing) {
    Write-Host "Opening $path"
    Start-Process $path
}

if ($missing) {
    Write-Host ""
    Write-Host "Some report files were not present:" -ForegroundColor Yellow
    foreach ($path in $missing) {
        Write-Host $path
    }
}
