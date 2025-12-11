@echo off
echo Compiling Java...
if not exist bin mkdir bin

javac -d bin *.java

if %errorlevel% neq 0 (
    echo ❌ Build failed.
    pause
    exit /b
)

echo ✔ Build successful.
echo Running program...
java -cp bin TestEnemyLoader

pause
