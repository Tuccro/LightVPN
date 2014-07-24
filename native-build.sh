#!/bin/bash

rm -rf assets
ndk-build clean
ndk-build
mkdir assets
for app in iptables ip6tables ip
do
  for eabi in armeabi armeabi-v7a x86
  do
    mkdir -p assets/$eabi
	mv libs/$eabi/$app assets/$eabi/$app
  done
done
