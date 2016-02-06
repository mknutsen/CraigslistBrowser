#!/bin/bash
cd $cl
_now=$(date +"%m_%d_%Y")
_open=open_$_now.txt
echo "Starting backup to $_open"
cp open.txt $_open
. $db/dropbox_uploader.sh upload $_open /