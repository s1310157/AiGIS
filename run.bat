@echo off
rem Double-click launcher for AiGIS. Delegates to the robust PowerShell launcher.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-aigis.ps1"