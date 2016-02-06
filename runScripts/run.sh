#!/bin/bash

cd $cl
./CraigslistBrowser ~/config.txt
cat open.txt
cd
./upload.sh
./uploadClosed.sh