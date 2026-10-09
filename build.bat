@echo off
REM Bien dich va chay chuong trinh (Windows)
if exist out rmdir /s /q out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
del sources.txt
java -cp out netcafe.Main
