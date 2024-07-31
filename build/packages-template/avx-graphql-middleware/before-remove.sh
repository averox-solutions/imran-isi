#!/bin/bash -e

stopService avx-graphql-middleware || echo "avx-graphql-middleware could not be unregistered or stopped"
