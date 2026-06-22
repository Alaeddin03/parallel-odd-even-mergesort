#!/bin/bash
# Compile and run Parallel Odd-Even Mergesort
mkdir -p out
javac -d out src/main/java/sorter/*.java && java -cp out sorter.Main
