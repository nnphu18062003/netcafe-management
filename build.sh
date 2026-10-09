#!/bin/sh
# Biên dịch và chạy chương trình (Linux/macOS)
set -e
rm -rf out
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out netcafe.Main
