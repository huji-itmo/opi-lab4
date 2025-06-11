#!/bin/bash

gradle war
cp app/build/libs/*.war deployments/
