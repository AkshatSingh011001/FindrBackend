@echo off
call compile.bat
if errorlevel 1 exit /b 1
echo Open http://localhost:8080 in your browser. Keep this window open.
java -cp bin com.findr.ServerMain
pause
