# Averox Web Grails 3 Deployment Procedure

Upgrade Gradle and Grails

```
# Install SDKMan if you don't have it yet.

curl -s "https://get.sdkman.io" | bash

# Install Gradle
sdk install gradle 5.1.1

# Install Grails
sdk install grails 3.3.9
```

### Development

Build `avx-common-message`

```
cd /averox/avx-common-message

./deploy.sh
```

Build `avx-common-web`

```
cd averox/avx-common-web

./deploy.sh
```

Build and run `avx-web`

```
cd averox/averox-web

# Make sure you don't have old libs lying around. Might cause issues.
# You need to to this only once to cleanup lib dir.

rm lib/*

./build.sh

# This will listen on port 8989 so you need to adjust your nginx config.
# If you've setup your nginx config to avx-web dev, you don't need to do anything.

./run.sh

```

To run unit tests: `grails test-app --stacktrace`

### Production

To package the application for production:

1. Compile the application and package it use `grails assemble`
2. You now have the file `build/libs/averox-0.10.0.war`
3. Create a new directory `mkdir exploded`
4. Navigate to that directory `cd exploded`
5. Extract the war content `jar -xvf ../build/libs/averox-0.10.0.war`
6. Package the content of the new directory in a debian package then add service configuration. Install into `/usr/share/avx-web`.
7. Use the systemd service file provided in build/package-template/avx-web. App will be listening on port 8080
8. To do custom config, edit `/usr/share/avx-web/WEB-INF/classes/averox.properties`
Don't forget to use full directories path and replace the dot before the org with the full path to the exploded war.
