#!/usr/bin/env bash

script_dir=$(dirname "$0")
cd $script_dir
cd ..

clj -X:test
