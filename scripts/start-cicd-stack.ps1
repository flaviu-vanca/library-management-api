$ErrorActionPreference = 'Stop'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI is not installed or not on PATH."
}

try {
    docker info | Out-Null
}
catch {
    throw "Docker Desktop is not running. Start Docker Desktop first, then re-run this script."
}

docker compose up -d --build

Write-Host ""
Write-Host "Supporting CI/CD services started."
Write-Host "Existing Jenkins UI : http://localhost:8080"
Write-Host "SonarQube           : http://localhost:9000"
Write-Host "Deployed app URL    : http://localhost:8082"
Write-Host ""
Write-Host "This compose stack does not start Jenkins."
Write-Host "It starts SonarQube, PostgreSQL, and the optional webhook relay for your Jenkins on port 8080."
Write-Host "If you want real GitHub push triggers, set SMEE_URL in .env and configure the matching GitHub webhook."
