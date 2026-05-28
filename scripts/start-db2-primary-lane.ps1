[CmdletBinding()]
param(
    [string]$EnvTemplate = (Join-Path $PSScriptRoot 'daytrader-db2.env.example'),
    [string]$GeneratedEnvFile = (Join-Path $PSScriptRoot '..\target\daytrader-db2.env'),
    [string]$ComposeFile = (Join-Path $PSScriptRoot '..\compose.db2.yml'),
    [string]$ProjectName = 'daytrader8-db2',
    [int]$StartupTimeoutSeconds = 900,
    [switch]$ResetData
)

$ErrorActionPreference = 'Stop'

function Read-EnvFile {
    param([string]$Path)

    $values = [ordered]@{}
    foreach ($line in Get-Content -Path $Path) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) {
            continue
        }

        $parts = $line -split '=', 2
        if ($parts.Count -ne 2) {
            continue
        }

        $values[$parts[0].Trim()] = $parts[1]
    }

    return $values
}

function Write-EnvFile {
    param(
        [string]$Path,
        [System.Collections.Specialized.OrderedDictionary]$Values
    )

    $content = foreach ($entry in $Values.GetEnumerator()) {
        "{0}={1}" -f $entry.Key, $entry.Value
    }

    Set-Content -Path $Path -Value $content -Encoding ascii
}

function New-Db2Password {
    $guid = [guid]::NewGuid().ToString('N').Substring(0, 10)
    return "Dt8!${guid}"
}

$repoRoot = Split-Path -Parent $PSScriptRoot

if (-not (Test-Path $EnvTemplate)) {
    throw "DB2 env template not found: $EnvTemplate"
}

if (-not (Test-Path $ComposeFile)) {
    throw "DB2 compose file not found: $ComposeFile"
}

$templateValues = Read-EnvFile -Path $EnvTemplate
$resolvedValues = [ordered]@{}

foreach ($entry in $templateValues.GetEnumerator()) {
    $resolvedValues[$entry.Key] = $entry.Value
}

if (Test-Path $GeneratedEnvFile) {
    foreach ($entry in (Read-EnvFile -Path $GeneratedEnvFile).GetEnumerator()) {
        $resolvedValues[$entry.Key] = $entry.Value
    }
}

if ([string]::IsNullOrWhiteSpace($resolvedValues['DAYTRADER_DB2_PASSWORD'])) {
    $resolvedValues['DAYTRADER_DB2_PASSWORD'] = New-Db2Password
}

$dbDirectory = $resolvedValues['DAYTRADER_DB2_DATABASE_DIR']
if (-not [System.IO.Path]::IsPathRooted($dbDirectory)) {
    $dbDirectory = Join-Path $repoRoot $dbDirectory
}

$resolvedValues['DAYTRADER_DB2_DATABASE_DIR'] = $dbDirectory

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $GeneratedEnvFile) | Out-Null
New-Item -ItemType Directory -Force -Path $dbDirectory | Out-Null

Write-EnvFile -Path $GeneratedEnvFile -Values $resolvedValues

$runtimeEnvScript = Join-Path $repoRoot 'target\daytrader-db2-app-env.ps1'
$runtimeEnvScriptContent = @(
    ('$env:SPRING_DATASOURCE_URL = ''jdbc:db2://{0}:{1}/{2}''' -f $resolvedValues['DAYTRADER_DB2_HOST'], $resolvedValues['DAYTRADER_DB2_PORT'], $resolvedValues['DAYTRADER_DB2_NAME']),
    '$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = ''com.ibm.db2.jcc.DB2Driver''',
    ('$env:SPRING_DATASOURCE_USERNAME = ''{0}''' -f $resolvedValues['DAYTRADER_DB2_USER']),
    ('$env:SPRING_DATASOURCE_PASSWORD = ''{0}''' -f $resolvedValues['DAYTRADER_DB2_PASSWORD']),
    ('$env:DAYTRADER_DB_USER = ''{0}''' -f $resolvedValues['DAYTRADER_DB2_USER']),
    ('$env:DAYTRADER_DB_PASSWORD = ''{0}''' -f $resolvedValues['DAYTRADER_DB2_PASSWORD'])
)
Set-Content -Path $runtimeEnvScript -Value $runtimeEnvScriptContent -Encoding ascii

$composeArgs = @(
    'compose',
    '--project-name', $ProjectName,
    '--env-file', $GeneratedEnvFile,
    '-f', $ComposeFile
)

if ($ResetData) {
    docker @composeArgs down --volumes --remove-orphans | Out-Host
}

docker @composeArgs up -d | Out-Host

$containerName = $resolvedValues['DAYTRADER_DB2_CONTAINER_NAME']
$deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
$status = ''

do {
    $status = docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $containerName 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to inspect DB2 container '$containerName'."
    }

    if ($status -eq 'healthy') {
        break
    }

    if ($status -eq 'unhealthy' -or $status -eq 'exited') {
        docker logs --tail 200 $containerName | Out-Host
        throw "DB2 container '$containerName' entered status '$status'."
    }

    Start-Sleep -Seconds 5
} while ((Get-Date) -lt $deadline)

if ($status -ne 'healthy') {
    docker logs --tail 200 $containerName | Out-Host
    throw "Timed out waiting for DB2 container '$containerName' to become healthy."
}

$appPort = $resolvedValues['DAYTRADER_APP_PORT']
Write-Host "DB2 is ready on $($resolvedValues['DAYTRADER_DB2_HOST']):$($resolvedValues['DAYTRADER_DB2_PORT'])/$($resolvedValues['DAYTRADER_DB2_NAME'])."
Write-Host "Generated compose env: $GeneratedEnvFile"
Write-Host "Generated app env script: $runtimeEnvScript"
Write-Host "Start the packaged WAR with: . $runtimeEnvScript; java -jar target\\io.openliberty.sample.daytrader8.war --server.port=$appPort"
