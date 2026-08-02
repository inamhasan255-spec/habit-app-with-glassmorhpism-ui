@echo off
REM Set up Google Services JSON from environment variables or .env file

where python3 >nul 2>nul
if %errorlevel% equ 0 (
    python3 "%~dp0setup_google_services.py"
    goto end
)

where python >nul 2>nul
if %errorlevel% equ 0 (
    python "%~dp0setup_google_services.py"
    goto end
)

echo Error: Python is required to run the google-services configuration setup.
exit /b 1

:end
exit /b 0
