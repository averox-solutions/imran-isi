# Install



Run the script to install all dependencies:
```
cd ~/src/avx-graphql-server
sudo ./install-hasura.sh
```

#### Update libs
```
cd ~/src/avx-common-message; 
./deploy.sh; 
cd ~/src/akka-avx-apps; 
sbt update; 
cd ~/src/avx-common-web; 
./deploy.sh; 
cd ~/src/averox-web/; 
./build.sh; 
```


#### Run Akka from source
```
cd ~/src/akka-avx-apps/; 
./run-dev.sh
```

#### Run BBB-web from source
```
cd ~/src/averox-web/; 
./run-dev.sh;
```

#### Run Html5 from source
```
cd ~/src/averox-html5/;
./run-dev.sh;
```



### Hasura Console
http://avx30.avxvm.imdt.com.br:8085/console

password: averox

### Client for tests:
```
cd  ~/src/avx-graphql-client-test
npm install
npm start
```

https://avx30.avxvm.imdt.com.br/graphql-test

- Join in a meeting, copy the param `?sessionToken=xxx` and append it to the URL above