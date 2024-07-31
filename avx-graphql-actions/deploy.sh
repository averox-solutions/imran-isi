#!/usr/bin/env bash

cd "$(dirname "$0")"

for var in "$@"
do
    if [[ $var == --reset ]] ; then
    	echo "Performing a full reset..."
      sudo rm -rf node_modules
    fi
done

if [ ! -d ./node_modules ] ; then
  sudo npm ci --no-progress
fi

sudo npm run build

# handle renaming circa dec 2023
if [[ -d /usr/local/averox/avx-graphql-actions-adapter-server ]] ; then
    sudo systemctl stop avx-graphql-actions-adapter-server
    sudo rm -f /usr/lib/systemd/system/avx-graphql-actions-adapter-server.service
    sudo systemctl daemon-reload
    sudo rm -rf /usr/local/averox/avx-graphql-actions-adapter-server
fi

sudo mv -f dist/index.js dist/avx-graphql-actions.js
sudo cp -rf dist/* /usr/local/averox/avx-graphql-actions
sudo systemctl restart avx-graphql-actions
echo ''
echo ''
echo '----------------'
echo 'avx-graphql-actions updated'
