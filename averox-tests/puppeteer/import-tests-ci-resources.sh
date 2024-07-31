#!/bin/bash -e

testDir=$PWD/averox-tests/puppeteer/;

echo "Cloning avx-ci-tests-resources repo...";
git clone https://github.com/averox/avx-ci-test-resources.git;
echo "avx-ci-tests-resources has been imported.";

sleep 2;
echo "Importing browser media files...";
mv -f avx-ci-test-resources/2.3/media $testDir;

if [[ $REGRESSION_TESTING = true ]]; then
    echo "Importing Visual Regressions Testing Files...";
    sleep 1;
    mv -f avx-ci-test-resources/2.3/__image_snapshots__ $testDir;
    echo "Visual Regressions Testing Files has been imported."
fi
rm -rf avx-ci-test-resources;