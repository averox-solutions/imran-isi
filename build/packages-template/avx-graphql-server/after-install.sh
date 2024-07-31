#!/bin/bash -e

case "$1" in
  configure|upgrade|1|2)

  fc-cache -f

  runuser -u postgres -- psql -c "alter user postgres password 'avx_graphql'"
  runuser -u postgres -- psql -c "drop database if exists avx_graphql with (force)"
  runuser -u postgres -- psql -c "create database avx_graphql WITH TEMPLATE template0 LC_COLLATE 'C.UTF-8'"
  runuser -u postgres -- psql -c "alter database avx_graphql set timezone to 'UTC'"
  runuser -u postgres -- psql -U postgres -d avx_graphql -q -f /usr/share/avx-graphql-server/avx_schema.sql --set ON_ERROR_STOP=on

  DATABASE_NAME="hasura_app"
  DB_EXISTS=$(runuser -u postgres -- psql -U postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DATABASE_NAME'")
  if [ "$DB_EXISTS" = '1' ]
  then
      echo "Database $DATABASE_NAME already exists"
  else
      runuser -u postgres -- psql -c "create database hasura_app"
      echo "Database $DATABASE_NAME created"
  fi

  echo "Postgresql configured"

  #Generate a random password to Hasura to improve security
  HASURA_ADM_PASSWORD=$(grep '^HASURA_GRAPHQL_ADMIN_SECRET=' /etc/default/avx-graphql-server | cut -d '=' -f 2)
  if [ "$HASURA_ADM_PASSWORD" = "averox" ]; then
    echo "Set a random password to Hasura replacing the default 'averox'"
    HASURA_RANDOM_ADM_PASSWORD=$(openssl rand -base64 32 | sed 's/=//g' | sed 's/+//g' | sed 's/\///g')
    sed -i "s/HASURA_GRAPHQL_ADMIN_SECRET=averox/HASURA_GRAPHQL_ADMIN_SECRET=$HASURA_RANDOM_ADM_PASSWORD/g" /etc/default/avx-graphql-server
    HASURA_ADM_PASSWORD="$HASURA_RANDOM_ADM_PASSWORD"
  fi

  sed -i "s/^admin_secret: .*/admin_secret: $HASURA_ADM_PASSWORD/g" /usr/share/avx-graphql-server/config.yaml

  if [ ! -f /.dockerenv ]; then
    systemctl enable avx-graphql-server.service
    systemctl daemon-reload
    restartService avx-graphql-server || echo "avx-graphql-server service could not be registered or started"

    #Check if Hasura is ready before applying metadata
    HASURA_PORT=8085
    while ! netstat -tuln | grep ":$HASURA_PORT " > /dev/null; do
        echo "Waiting for Hasura's port ($HASURA_PORT) to be ready..."
        sleep 1
    done

    # Apply BBB metadata in Hasura
    cd /usr/share/avx-graphql-server
    /usr/local/bin/hasura metadata apply --skip-update-check
    cd ..
    rm -rf /usr/share/avx-graphql-server/metadata
  fi

  ;;

  abort-upgrade|abort-remove|abort-deconfigure)
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac
