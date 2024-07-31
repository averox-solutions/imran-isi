#!/bin/bash -e

stopService avx-graphql-server || echo "avx-graphql-server could not be unregistered or stopped"
