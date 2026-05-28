param(
    [string]$OutputPath = "target\readiness-evidence.md",
    [string]$SurefireDir = "target\surefire-reports",
    [string]$PlaywrightReportDir = "e2e\playwright-report",
    [string]$PlaywrightResultsPath = "e2e\test-results"
)

$ErrorActionPreference = 'Stop'

function Get-SurefireSummary {
    param([string]$Directory)

    if (-not (Test-Path $Directory)) {
        return [pscustomobject]@{ Passed = 0; Failed = 0; Errors = 0; Skipped = 0; Suites = @() }
    }

    $suites = Get-ChildItem -Path $Directory -Filter 'TEST-*.xml' | ForEach-Object {
        $xml = [xml](Get-Content -Raw -Path $_.FullName)
        [pscustomobject]@{
            Name = $xml.testsuite.name
            Tests = [int]$xml.testsuite.tests
            Failures = [int]$xml.testsuite.failures
            Errors = [int]$xml.testsuite.errors
            Skipped = [int]$xml.testsuite.skipped
        }
    }

    return [pscustomobject]@{
        Passed = ($suites | Measure-Object -Property Tests -Sum).Sum - ($suites | Measure-Object -Property Failures -Sum).Sum - ($suites | Measure-Object -Property Errors -Sum).Sum - ($suites | Measure-Object -Property Skipped -Sum).Sum
        Failed = ($suites | Measure-Object -Property Failures -Sum).Sum
        Errors = ($suites | Measure-Object -Property Errors -Sum).Sum
        Skipped = ($suites | Measure-Object -Property Skipped -Sum).Sum
        Suites = $suites
    }
}

function Get-PlaywrightSummary {
    param(
        [string]$ReportDirectory,
        [string]$ResultsPath
    )

    [pscustomobject]@{
        HtmlReportExists = Test-Path $ReportDirectory
        ResultsExist = Test-Path $ResultsPath
    }
}

$surefire = Get-SurefireSummary -Directory $SurefireDir
$playwright = Get-PlaywrightSummary -ReportDirectory $PlaywrightReportDir -ResultsPath $PlaywrightResultsPath

$outputDirectory = Split-Path -Parent $OutputPath
if ($outputDirectory -and -not (Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}

$lines = @(
    '# Readiness Evidence',
    '',
    "Generated: $((Get-Date).ToUniversalTime().ToString('yyyy-MM-ddTHH:mm:ssZ'))",
    '',
    '## Surefire Summary',
    "- Passed: $($surefire.Passed)",
    "- Failed: $($surefire.Failed)",
    "- Errors: $($surefire.Errors)",
    "- Skipped: $($surefire.Skipped)",
    ''
)

if ($surefire.Suites.Count -gt 0) {
    $lines += '### Suites'
    foreach ($suite in $surefire.Suites) {
        $lines += "- $($suite.Name): tests=$($suite.Tests), failures=$($suite.Failures), errors=$($suite.Errors), skipped=$($suite.Skipped)"
    }
    $lines += ''
}

$lines += @(
    '## Playwright Summary',
    "- HTML report present: $($playwright.HtmlReportExists)",
    "- Test results present: $($playwright.ResultsExist)"
)

Set-Content -Path $OutputPath -Value $lines