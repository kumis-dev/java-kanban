#!/bin/bash
cd "$(dirname "$0")"
java -jar taskmanager.jar
echo "Нажми Enter для выхода"
read
