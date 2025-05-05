#!/bin/bash

# Create build directory
mkdir -p build

# Compile Kotlin files
echo "Compiling Kotlin files..."
kotlinc app/src/main/java/com/wordgame/demo/WordGameDemo.kt -include-runtime -d build/WordGameDemo.jar

# Run the game
echo "Starting Word Game Demo..."
java -jar build/WordGameDemo.jar