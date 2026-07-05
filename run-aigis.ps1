# =============================================================================
#  Robust AiGIS launcher
#  - Finds a Java runtime (JAVA_HOME or PATH)
#  - Finds the AiGIS.jar (built output preferred, else the committed jar)
#  - Auto-discovers an asteroid data folder (one containing AiGIS_setting.txt,
#    preferring complete folders that have a Shapemodel/ directory)
#  - Writes AiGIS.properties with a correctly Java-escaped defaultDataPath,
#    PRESERVING any previously chosen language (so the first-launch language
#    dialog only appears once)
#  - Launches with the native JOGL libraries on java.library.path
#
#  This makes the app start with a model + descriptions every time, regardless
#  of where the data lives (incl. non-ASCII paths) or whether 'out/' was wiped
#  by a rebuild. Run it from anywhere; double-click run.bat for convenience.
# =============================================================================
$ErrorActionPreference = 'Stop'
$here = Split-Path -Parent $MyInvocation.MyCommand.Path   # the src/ folder
Set-Location $here

function Fail($msg) { Write-Host "[AiGIS] ERROR: $msg" -ForegroundColor Red; Read-Host "Press Enter to close"; exit 1 }

# --- 1. Java runtime ---------------------------------------------------------
# Detect without executing java (running it can trip ErrorActionPreference=Stop
# in Windows PowerShell 5.1 because java -version writes to stderr).
$java = $null
if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    $java = "$env:JAVA_HOME\bin\java.exe"
} else {
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) { $java = $cmd.Source }
}
if (-not $java) { Fail "Java not found. Install Java 8+ or set JAVA_HOME." }

# --- 2. Locate the jar (built output first, then committed) ------------------
$jarRel = @('out\win\app\AiGIS.jar', 'out\jar\AiGIS.jar', 'AiGIS.jar') |
    Where-Object { Test-Path (Join-Path $here $_) } | Select-Object -First 1
if (-not $jarRel) { Fail "AiGIS.jar not found. Build it first (make.sh) or restore the committed jar." }
$jarPath = Join-Path $here $jarRel
$jarDir  = Split-Path -Parent $jarPath

# --- 3. Native JOGL libraries ------------------------------------------------
$natives = @((Join-Path $jarDir 'libs\win'), (Join-Path $here 'libs\win')) |
    Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $natives) { Fail "Native libraries (libs\win) not found." }

# --- 4. Discover an asteroid data folder ------------------------------------
# Search the project root (parent of src) for AiGIS_setting.txt; prefer folders
# that actually contain a 3D model (Shapemodel/).
$projectRoot = Split-Path -Parent $here
$data = $null; $dataFallback = $null
Get-ChildItem -Path $projectRoot -Recurse -Depth 4 -Filter 'AiGIS_setting.txt' -ErrorAction SilentlyContinue |
    ForEach-Object {
        $d = $_.Directory.FullName
        if (Test-Path (Join-Path $d 'Shapemodel')) { if (-not $data) { $data = $d } }
        elseif (-not $dataFallback) { $dataFallback = $d }
    }
if (-not $data) { $data = $dataFallback }

# --- 5. Write AiGIS.properties (next to the jar = the app's working dir) -----
# Java .properties are ISO-8859-1 with \uXXXX escapes; replicate Java's escaping.
function ConvertTo-PropValue([string]$s) {
    $sb = New-Object System.Text.StringBuilder
    foreach ($ch in $s.ToCharArray()) {
        $code = [int]$ch
        if     ($ch -eq '\') { [void]$sb.Append('\\') }
        elseif ($ch -eq ':') { [void]$sb.Append('\:') }
        elseif ($ch -eq '=') { [void]$sb.Append('\=') }
        elseif ($code -lt 0x20 -or $code -gt 0x7e) { [void]$sb.Append('\u' + $code.ToString('x4')) }
        else   { [void]$sb.Append($ch) }
    }
    $sb.ToString()
}

$propPath = Join-Path $jarDir 'AiGIS.properties'
# Preserve a previously chosen language so the dialog does not reappear.
$locale = $null
if (Test-Path $propPath) {
    $m = Select-String -Path $propPath -Pattern '^locale=(.+)$' | Select-Object -First 1
    if ($m) { $locale = $m.Matches[0].Groups[1].Value.Trim() }
}

$lines = @('#AiGIS')
if ($data)   { $lines += "defaultDataPath=$(ConvertTo-PropValue $data)" }
if ($locale) { $lines += "locale=$locale" }
# ASCII encoding keeps the \uXXXX escapes literal (what java.util.Properties expects)
[System.IO.File]::WriteAllLines($propPath, $lines, [System.Text.Encoding]::ASCII)

if ($data) { Write-Host "[AiGIS] data: $data" } else { Write-Host "[AiGIS] no data folder found; the app will show an open dialog." -ForegroundColor Yellow }

# --- 6. Launch ---------------------------------------------------------------
# Relax error handling: the app writes to stderr during normal operation, which
# Windows PowerShell 5.1 would otherwise surface as an error.
$ErrorActionPreference = 'Continue'
Set-Location $jarDir
& $java "-Djava.library.path=$natives" -jar $jarPath 2>&1 | Out-Null