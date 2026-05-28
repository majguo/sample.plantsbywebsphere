[CmdletBinding()]
param(
    [string]$GeneratedEnvFile = (Join-Path $PSScriptRoot '..\target\daytrader-db2.env'),
    [string]$ComposeFile = (Join-Path $PSScriptRoot '..\compose.db2.yml'),
    [string]$ProjectName = 'daytrader8-db2',
    [switch]$DeleteData
)

$ErrorActionPreference = 'Stop'

$args = @(
    'compose',
    '--project-name', $ProjectName,
    '--env-file', $GeneratedEnvFile,
    '-f', $ComposeFile,
    'down',
    '--remove-orphans'
)

if ($DeleteData) {
    $args += '--volumes'
}

docker @args
