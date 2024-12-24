#!/bin/sh

sshpass -f "/home/huji/.helios_pass" scp -P 2222 ../build/libs/*.war s389491@helios.cs.ifmo.ru:~/wildfly/wildfly-20.0.1.Final/standalone/deployments
