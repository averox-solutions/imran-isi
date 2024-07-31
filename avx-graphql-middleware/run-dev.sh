#!/bin/bash

sudo systemctl stop avx-graphql-middleware
set -a # Automatically export all variables
source /etc/default/avx-graphql-middleware
set +a # Stop automatically exporting
go run cmd/avx-graphql-middleware/main.go  --signal SIGTERM
