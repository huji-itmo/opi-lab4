#!/bin/sh

sshpass -f "/home/huji/.helios_pass" ssh -p 2222 -N -L 8080:localhost:29043 -L 9990:localhost:30953 s389491@helios.cs.ifmo.ru
