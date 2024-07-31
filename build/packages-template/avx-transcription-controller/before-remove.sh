#!/bin/bash -e

stopService avx-transcription-controller || echo "avx-transcription-controller could not be unregistered or stopped"
