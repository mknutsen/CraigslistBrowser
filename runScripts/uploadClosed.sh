#!/bin/bash
cd $cl
_now=$(date +"%m_%d_%Y")
_closed=closed_$_now.txt
echo "Starting backup to $_closed"
cp closed.txt $_closed
. $db/dropbox_uploader.sh upload $_closed /
