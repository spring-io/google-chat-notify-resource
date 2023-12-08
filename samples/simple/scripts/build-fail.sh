#!/bin/sh

echo "Simulating build failure"
echo -n "additional build output" > test/build-output.txt
exit 1
