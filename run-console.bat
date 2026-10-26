@echo off
call compile.bat
if errorlevel 1 exit /b 1
java -cp bin com.findr.Main
pause
