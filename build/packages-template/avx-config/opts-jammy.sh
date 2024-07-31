. ./opts-global.sh

AKKA_APPS="avx-fsesl-akka,avx-apps-akka"
OPTS="$OPTS -t deb -d netcat-openbsd,stun-client,avx-html5,avx-playback-presentation,avx-playback,avx-freeswitch-core,$AKKA_APPS,yq"
