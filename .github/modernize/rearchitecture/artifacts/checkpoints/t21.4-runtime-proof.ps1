$ErrorActionPreference = 'Stop'

$base = 'http://127.0.0.1:19101/daytrader'
$results = New-Object System.Collections.Generic.List[object]

function Add-Result {
    param(
        [string]$Category,
        [string]$Path,
        [int]$Status,
        [bool]$Pass,
        [string]$Note
    )

    $results.Add([pscustomobject]@{
        category = $Category
        path = $Path
        status = $Status
        pass = $Pass
        note = $Note
    }) | Out-Null
}

function Invoke-Probe {
    param(
        [string]$Category,
        [string]$Path,
        [string]$Expected,
        [Microsoft.PowerShell.Commands.WebRequestSession]$Session = $null,
        [string]$ContentTypeLike = $null
    )

    try {
        $response = if ($null -ne $Session) {
            Invoke-WebRequest -Uri ($base + $Path) -WebSession $Session -MaximumRedirection 5
        } else {
            Invoke-WebRequest -Uri ($base + $Path) -MaximumRedirection 5
        }

        $content = [string]$response.Content
        $status = [int]$response.StatusCode
        $pass = ($status -eq 200)

        if ($Expected) {
            $pass = $pass -and $content.Contains($Expected)
        }

        if ($ContentTypeLike) {
            $pass = $pass -and ($response.Headers['Content-Type'] -like $ContentTypeLike)
        }

        $note = if ($pass) {
            'ok'
        } else {
            "expected='$Expected'; contentType='" + $response.Headers['Content-Type'] + "'"
        }

        Add-Result -Category $Category -Path $Path -Status $status -Pass $pass -Note $note
    } catch {
        if ($_.Exception.Response) {
            $response = $_.Exception.Response
            $status = [int]$response.StatusCode
            $body = if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
                $_.ErrorDetails.Message
            } else {
                $_.Exception.Message
            }
            $note = if ($body.Length -gt 180) {
                $body.Substring(0, 180)
            } else {
                $body
            }
            Add-Result -Category $Category -Path $Path -Status $status -Pass $false -Note $note
        } else {
            Add-Result -Category $Category -Path $Path -Status -1 -Pass $false -Note $_.Exception.Message
        }
    }
}

try {
    $bootstrap = Invoke-WebRequest -Uri ($base + '/config?action=buildDB') -MaximumRedirection 5
    Add-Result -Category 'bootstrap' -Path '/config?action=buildDB' -Status ([int]$bootstrap.StatusCode) -Pass ($bootstrap.Content.Contains('TradeBuildDB: Building DayTrader Database')) -Note 'anonymous buildDB probe'
} catch {
    $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { -1 }
    $note = if ($status -eq 401) { 'seed path already closed on this lane' } else { $_.Exception.Message }
    Add-Result -Category 'bootstrap' -Path '/config?action=buildDB' -Status $status -Pass ($status -eq 401) -Note $note
}

$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$login = Invoke-WebRequest -Uri ($base + '/app') -Method Post -Body @{ action = 'login'; uid = 'uid:0'; passwd = 'xxx' } -WebSession $session
Add-Result -Category 'auth' -Path '/app?action=login' -Status ([int]$login.StatusCode) -Pass ($login.Content.Contains('Welcome to DayTrader')) -Note 'canonical operator login'

$docs = @(
    @{ path = '/docs/tradeFAQ.html'; expected = 'Frequently Asked Questions'; ct = 'text/html*' },
    @{ path = '/docs/tradeversion.html'; expected = 'DayTrader Version'; ct = 'text/html*' },
    @{ path = '/docs/benchmarking.html'; expected = 'Benchmarking Details'; ct = 'text/html*' },
    @{ path = '/docs/documentation.html'; expected = 'Technical Documentation'; ct = 'text/html*' },
    @{ path = '/docs/glossary.html'; expected = 'Technical Documentation'; ct = 'text/html*' },
    @{ path = '/docs/rtCharacterisitics.html'; expected = 'Trade Runtime and Database Usage Characteristics'; ct = 'text/html*' },
    @{ path = '/docs/tradeTech.pdf'; expected = ''; ct = 'application/pdf*' },
    @{ path = '/docs/tradeUML.pdf'; expected = ''; ct = 'application/pdf*' }
)

foreach ($probe in $docs) {
    Invoke-Probe -Category 'docs' -Path $probe.path -Expected $probe.expected -ContentTypeLike $probe.ct
}

$publicAlternate = @(
    @{ path = '/index.faces'; expected = 'DayTrader' },
    @{ path = '/welcome.faces'; expected = 'DayTrader Login' },
    @{ path = '/register.faces'; expected = 'DayTrader' },
    @{ path = '/configure.faces'; expected = 'Configuration Utilities' },
    @{ path = '/web_prmtv.faces'; expected = 'DayTrader Primitives' },
    @{ path = '/docs/tradeFAQ.faces'; expected = 'DayTrader' },
    @{ path = '/PingJsf.faces'; expected = 'PingJSF' },
    @{ path = '/PingCDIJSF.faces'; expected = 'DayTrader PingJSF' }
)

foreach ($probe in $publicAlternate) {
    Invoke-Probe -Category 'alternate-public' -Path $probe.path -Expected $probe.expected
}

$protectedAlternate = @(
    @{ path = '/tradehome.faces'; expected = 'DayTrader Home' },
    @{ path = '/account.faces'; expected = 'DayTrader Account' },
    @{ path = '/marketSummary.faces'; expected = 'Market Summary' },
    @{ path = '/portfolio.faces'; expected = 'DayTrader Portfolio' },
    @{ path = '/quote.faces'; expected = 'DayTrader' }
)

foreach ($probe in $protectedAlternate) {
    Invoke-Probe -Category 'alternate-auth' -Path $probe.path -Expected $probe.expected -Session $session
}

Invoke-Probe -Category 'image-direct' -Path '/welcomeImg.jsp' -Expected 'DayTrader Login'
Invoke-Probe -Category 'image-direct' -Path '/registerImg.jsp' -Expected 'DayTrader Registration'

$primitiveRoutes = @(
    '/web_prmtv.html',
    '/PingHtml.html',
    '/servlet/ExplicitGC',
    '/servlet/PingServlet',
    '/servlet/PingServletCDI',
    '/servlet/PingServletCDIBeanManagerViaJNDI',
    '/servlet/PingServletCDIBeanManagerViaCDICurrent',
    '/servlet/PingServletWriter',
    '/servlet/PingServlet2Include',
    '/servlet/PingServlet2Servlet',
    '/PingJsp.jsp',
    '/PingJspEL.jsp',
    '/servlet/PingServlet2Jsp',
    '/servlet/PingServlet2PDF',
    '/servlet/PingServlet2DB',
    '/PingJsf.faces',
    '/PingCDIJSF.faces',
    '/servlet/PingSession1',
    '/servlet/PingSession2',
    '/servlet/PingSession3',
    '/servlet/PingJDBCRead',
    '/servlet/PingJDBCRead2JSP',
    '/servlet/PingJDBCWrite',
    '/servlet/PingServlet2JNDI',
    '/servlet/PingUpgradeServlet',
    '/PingWebSocketTextSync.html',
    '/PingWebSocketTextAsync.html',
    '/PingWebSocketBinary.html',
    '/PingWebSocketJson.html',
    '/servlet/PingManagedThread',
    '/servlet/PingManagedExecutor',
    '/servlet/PingJSONP',
    '/ejb3/PingServlet2Session',
    '/ejb3/PingServlet2SessionLocal',
    '/ejb3/PingServlet2Entity',
    '/ejb3/PingServlet2Session2Entity',
    '/ejb3/PingServlet2Session2Entity2JSP',
    '/ejb3/PingServlet2Session2EntityCollection',
    '/ejb3/PingServlet2Session2CMROne2One',
    '/ejb3/PingServlet2Session2CMROne2Many',
    '/ejb3/PingServlet2MDBQueue',
    '/ejb3/PingServlet2MDBTopic',
    '/ejb3/PingServlet2TwoPhase'
)

foreach ($route in $primitiveRoutes) {
    Invoke-Probe -Category 'primitive' -Path $route -Expected ''
}

$configUpdate = Invoke-WebRequest -Uri ($base + '/config') -Method Post -Body @{ action = 'updateConfig'; WebInterface = '1' } -WebSession $session
Add-Result -Category 'image-config' -Path '/config?action=updateConfig&WebInterface=1' -Status ([int]$configUpdate.StatusCode) -Pass ([int]$configUpdate.StatusCode -eq 200) -Note 'switch to JSP-Images'

$imageActions = @(
    @{ path = '/app?action=home'; expected = 'menuHome.gif' },
    @{ path = '/app?action=account'; expected = 'menuHome.gif' },
    @{ path = '/app?action=portfolio'; expected = 'menuHome.gif' },
    @{ path = '/app?action=quotes&symbols=s:0,s:1'; expected = 'menuHome.gif' },
    @{ path = '/app?action=buy&symbol=s:0&quantity=1'; expected = 'menuHome.gif' }
)

foreach ($probe in $imageActions) {
    Invoke-Probe -Category 'image-action' -Path $probe.path -Expected $probe.expected -Session $session
}

$summary = $results | Group-Object category | ForEach-Object {
    [pscustomobject]@{
        category = $_.Name
        passed = ($_.Group | Where-Object pass).Count
        failed = ($_.Group | Where-Object { -not $_.pass }).Count
    }
}

[pscustomobject]@{
    summary = $summary
    failures = @($results | Where-Object { -not $_.pass } | Select-Object category, path, status, note)
    totalPassed = @($results | Where-Object pass).Count
    totalFailed = @($results | Where-Object { -not $_.pass }).Count
} | ConvertTo-Json -Depth 6