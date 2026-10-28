$ErrorActionPreference = 'Stop'
$taskJavac = 'javac'; $taskJava = 'java'
if ($env:JAVA_HOME) { $taskJavac = Join-Path $env:JAVA_HOME 'bin\javac.exe'; $taskJava = Join-Path $env:JAVA_HOME 'bin\java.exe' }
$taskClasses = Join-Path $PSScriptRoot 'build\core-tests'
New-Item -ItemType Directory -Force $taskClasses | Out-Null
& $taskJavac -encoding UTF-8 -d $taskClasses (Join-Path $PSScriptRoot 'app\src\main\java\ru\photolens\app\PhotoMath.java') (Join-Path $PSScriptRoot 'app\src\main\java\ru\photolens\app\Labels.java') (Join-Path $PSScriptRoot 'tests\CoreCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
& $taskJava -cp $taskClasses ru.photolens.app.CoreCheck
if ($LASTEXITCODE -ne 0) { throw 'Checks failed' }
