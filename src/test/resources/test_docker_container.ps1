# Test Docker container locally with environment variables
$ErrorActionPreference = "Stop"

Write-Host "=== TESTING RESOLVEIT PRODUCTION DOCKER CONTAINER LOCALLY ==="

# 1. Clean up existing container if any
docker rm -f resolveit-app 2>$null | Out-Null

# 2. Extract local database password safely from local properties
$propsFile = Join-Path $PSScriptRoot "..\..\main\resources\application-local.properties"
$dbPassword = ""
if (Test-Path $propsFile) {
    $match = Select-String -Path $propsFile -Pattern "^spring\.datasource\.password=(.*)$"
    if ($match) {
        $dbPassword = $match.Matches.Groups[1].Value.Trim()
    }
}

Write-Host "Starting Docker container with environment variables..."
$containerId = docker run -d --name resolveit-app `
    -p 8080:8080 `
    -e PORT=8080 `
    -e DB_HOST=host.docker.internal `
    -e DB_PORT=3306 `
    -e DB_NAME=resolveit_db `
    -e DB_USERNAME=root `
    -e DB_PASSWORD="$dbPassword" `
    -e DB_SSL_MODE=false `
    resolveit:latest

Write-Host "Container started with ID: $containerId"

# 3. Wait for Spring Boot application inside container to start
$maxRetries = 30
$retryCount = 0
$isUp = $false

Write-Host "Waiting for application inside Docker container to become reachable on http://localhost:8080..."
while ($retryCount -lt $maxRetries) {
    Start-Sleep -Seconds 2
    $retryCount++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($response -and $response.StatusCode -eq 200) {
            $isUp = $true
            Write-Host "Application is UP and responding (HTTP 200 OK) after $($retryCount * 2) seconds!"
            break
        }
    } catch {
        # continue waiting
    }
}

if (-not $isUp) {
    Write-Host "Container failed to respond within timeout. Logs:"
    docker logs resolveit-app
    exit 1
}

# 4. Run end-to-end verification script against container
Write-Host "Executing end-to-end verification checks against Docker container..."
$verifyScript = Join-Path $PSScriptRoot "verify_app.ps1"
& powershell -ExecutionPolicy Bypass -File $verifyScript

$verifyExitCode = $LASTEXITCODE
if ($verifyExitCode -eq 0) {
    Write-Host "`n>>> DOCKER CONTAINER VERIFICATION COMPLETED WITH 100% SUCCESS! <<<"
} else {
    Write-Host "`n>>> DOCKER CONTAINER VERIFICATION FAILED! <<<"
    exit 1
}
