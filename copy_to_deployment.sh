#!/bin/bash

cd ..
gradle war
cp app/build/libs/*.war deployments/
