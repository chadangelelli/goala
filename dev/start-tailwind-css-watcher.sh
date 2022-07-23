#!/bin/bash

script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $script_dir
cd ..

npx tailwindcss \
      -i ./resources/public/css/__styles.css \
      -o ./resources/public/css/st5-styles.css \
      --watch
