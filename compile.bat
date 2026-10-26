@echo off
if not exist bin mkdir bin
javac -encoding UTF-8 -d bin -sourcepath src src\com\findr\Main.java src\com\findr\ServerMain.java src\com\findr\test\TestRunner.java
if errorlevel 1 (echo. & echo COMPILE FAILED - read the error above. & pause & exit /b 1)
echo Compiled OK.
