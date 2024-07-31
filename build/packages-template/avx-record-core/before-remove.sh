#!/bin/bash -e

stopService avx-record-core.timer || echo "avx-record-core could not be unregistered or stopped"

