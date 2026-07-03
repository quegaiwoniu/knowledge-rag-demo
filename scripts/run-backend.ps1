$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$jdkHome = "D:\SoftWare\IntelliJ IDEA 2026.1.3\jbr"
$mavenArgs = @("-s", ".mvn/settings.xml", "spring-boot:run")

if (-not (Test-Path -LiteralPath $jdkHome)) {
    throw "Stable JDK not found: $jdkHome"
}

$env:JAVA_HOME = $jdkHome
$env:Path = "$jdkHome\bin;$env:Path"

Set-Location $projectRoot

Write-Host "Using JAVA_HOME=$env:JAVA_HOME"
Write-Host "Starting backend with stable local JDK..."

mvn @mavenArgs
