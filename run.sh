#!/bin/bash
# Compile and run Parallel Odd-Even Mergesort
rm dataset.bin 
mkdir -p out
javac -d out src/main/java/sorter/*.java && java -cp out sorter.Main
