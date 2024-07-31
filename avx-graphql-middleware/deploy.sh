#!/bin/bash

./local-build.sh
sudo mv avx-graphql-middleware /usr/local/bin/avx-graphql-middleware
sudo systemctl restart avx-graphql-middleware
