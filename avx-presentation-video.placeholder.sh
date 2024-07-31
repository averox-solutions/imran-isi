#!/bin/sh
set -ex
RELEASE=5.0.0-beta.1
cat <<MSG
This tool downloads prebuilt packages built on Github Actions
The corresponding source can be browsed at https://github.com/averox/avx-presentation-video/tree/${RELEASE}
Build logs are at https://github.com/averox/avx-presentation-video/actions/workflows/package.yml?query=branch%3A${RELEASE}
MSG
curl -Lf -o avx-presentation-video.zip "https://github.com/averox/avx-presentation-video/releases/download/${RELEASE}/ubuntu-20.04.zip"
rm -rf avx-presentation-video
unzip -o avx-presentation-video.zip -d avx-presentation-video
