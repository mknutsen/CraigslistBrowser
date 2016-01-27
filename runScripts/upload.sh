#!/bin/bash
cd $cl
_now=$(date +"%m_%d_%Y")
_file=open_$_now.txt
echo "Starting backup to $_file..."
cp open.txt $_file
. $db/dropbox_uploader.sh upload $_file /
