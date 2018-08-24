#!/bin/bash
#1:localDir,2:localFile,3:remoteDir,4:pod,5:namespace,6:token,7:server
rm -rf $1
mkdir -p $1
if [ "${2##*.}"x = "zip"x ];
then
    unzip -q "$2" -d "$1"
elif [ "${2##*.}"x = "tar"x ];
then
    tar xfo "$2" -C "$1"
fi
for f in "$1"/*
    do
        kubectl cp "${f}" $5/$4:"$3" --token=$6 --server=$7 --insecure-skip-tls-verify=true
    done
rm -rf $1
