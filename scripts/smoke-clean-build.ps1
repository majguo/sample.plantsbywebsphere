Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')

Push-Location $repoRoot
try {
    & mvn -B liberty:stop
    if ($LASTEXITCODE -ne 0) {
        Write-Host "liberty:stop returned exit code $LASTEXITCODE; continuing with clean build."
    }

    & mvn -B -DskipTests clean package
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}