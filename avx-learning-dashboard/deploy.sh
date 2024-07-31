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
	npm install
fi

npm run build
cp -r build/* /var/averox/learning-dashboard
sudo systemctl restart nginx
echo ''
echo ''
echo '----------------'
echo 'avx-learning-dashboard updated'
