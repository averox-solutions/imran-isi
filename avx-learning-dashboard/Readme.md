Learning Analytics Dashboard will be accessible through https://yourdomain/learning-analytics-dashboard

# Dev Instructions

## Prepare destination directory

```
mkdir -p /var/averox/learning-dashboard
chown averox /var/averox/learning-dashboard/
```

## Build instructions

```
# verify we are in the avx-learning-dashboard directory ~/src/avx-learning-dashboard
pwd

if [ -d node_modules ]; then rm -r node_modules; fi
npm install
npm run build
cp -r build/* /var/averox/learning-dashboard
```

## Update nginx config

```
cp learning-dashboard.nginx /usr/share/averox/nginx/
```
