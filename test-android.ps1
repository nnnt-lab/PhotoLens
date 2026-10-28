$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & .\gradlew.bat testDebugUnitTest '-Pandroid.useAndroidX=true' --init-script tests/android-tests.init.gradle --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Android integration checks failed' }
} finally { Pop-Location }
