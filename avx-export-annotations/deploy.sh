#!/usr/bin/env bash
cd "$(dirname "$0")"

for var in "$@"
do
    if [[ $var == --reset ]] ; then
    	echo "Performing a full reset..."
      rm -rf node_modules
    fi
done

if [ ! -d ./node_modules ] ; then
	npm install --production
fi

sudo cp -r ./* /usr/local/averox/avx-export-annotations
sudo systemctl restart avx-export-annotations
echo ''
echo ''
echo '----------------'
echo 'avx-export-annotations updated'
