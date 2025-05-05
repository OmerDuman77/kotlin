#!/bin/bash

# Compile Java web server version
echo "Compiling Java web game..."
javac WebWordGame.java

# Run the web server
echo "Starting Web Word Game server on port 5000..."
java WebWordGame