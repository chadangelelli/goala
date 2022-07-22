#!/usr/bin/env bash

GOALA_LOGO=$(cat <<'LOGO_TEXT'
  ____             _       
 / ___| ___   __ _| | __ _ 
| |  _ / _ \ / _` | |/ _` |
| |_| | (_) | (_| | | (_| |
 \____|\___/ \__,_|_|\__,_|
LOGO_TEXT
)

BOLD='\033[1m'
BLUE='\033[0;34m'
RED='\033[0;31m'
GREEN='\033[0;32m'
ORANGE='\033[0;33m'
PURPLE='\033[0;35m'
NC='\033[0m'

INFO="${BLUE}[INFO]${NC}"
WARN="${GREEN}[WARNING]${NC}"
ERROR="${RED}[ERROR]${NC}"
DEBUG="${PURPLE}[DEBUG]${NC}"
