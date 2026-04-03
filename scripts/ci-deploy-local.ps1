param(
    [Parameter(Mandatory = $true)]
    [string]$AppContainer,
    [Parameter(Mandatory = $true)]
    [string]$AppImage,
    [Parameter(Mandatory = $true)]
    [string]$Tag,
    [int]$AppPort = 8082
)

$ErrorActionPreference = "Stop"

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
