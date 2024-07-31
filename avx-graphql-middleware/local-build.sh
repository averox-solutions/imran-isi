#!/bin/bash
CGO_ENABLED=0 go build -o avx-graphql-middleware cmd/avx-graphql-middleware/main.go
echo "Build of avx-graphql-middleware finished"
