#!/usr/bin/env bash

script_dir=$(dirname "$0")
cd $script_dir
cd ..

npx tailwindcss \
      -i ./resources/public/css/__styles.css \
      -o ./resources/public/css/styles.css \
      --watch
