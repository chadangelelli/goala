#!/usr/bin/env bash

script_dir=$(dirname "$0")
cd $script_dir
cd ..

source dev/formatting.sh

echo -e "${ORANGE}${GOALA_LOGO}${NC}\n"

clojure -M:repl
