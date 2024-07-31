#!/bin/bash -e

case "$1" in
   remove|failed-upgrade|abort-upgrade|abort-install|disappear|0|1)

   if [ -L /etc/nginx/sites-enabled/averox ]; then
     rm /etc/nginx/sites-enabled/averox
   fi

   ;;
   purge)
     deleteUser meteor
     deleteGroup meteor
   ;;
   upgrade)
   ;;
   *)
      echo "postinst called with unknown argument $1" >&2
   ;;
esac

