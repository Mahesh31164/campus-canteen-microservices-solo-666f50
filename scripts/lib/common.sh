#!/usr/bin/env bash
# Shared helpers for the canteen verify/run scripts.
set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
GRADLEW="$ROOT_DIR/gradlew"

# Service -> port map (kept in one place so verify-T0n scripts stay tiny)
SERVICE_DISCOVERY=8761
SERVICE_GATEWAY=8080
SERVICE_MENU=8081
SERVICE_ORDER=8082
SERVICE_AUTH=8083

# Public gateway URLs
API_MENU="http://localhost:8080/api/menu"
API_ORDER_DETAIL="http://localhost:8080/api/orders/1/detail"

_passed=0
_failed=0

ok()   { printf '  \033[32mPASS\033[0m  %s\n' "$*"; _passed=$((_passed + 1)); }
fail() { printf '  \033[31mFAIL\033[0m  %s\n' "$*"; _failed=$((_failed + 1)); }
info() { printf '\033[36m[..]\033[0m  %s\n' "$*"; }

summary() {
  printf '\n%2d passed, %2d failed\n' "$_passed" "$_failed"
  [ "$_failed" -eq 0 ]
}

die() { printf '\033[31mERROR\033[0m %s\n' "$*" >&2; exit 1; }

# --- ports ----------------------------------------------------------------
port_free() {
  ! (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null && return 0
  return 1
}
port_open() {
  (exec 3<>"/dev/tcp/127.0.0.1/$1") 2>/dev/null && return 0
  return 1
}
wait_port() { # wait_port PORT [tries]
  local tries="${2:-30}"
  for _ in $(seq 1 "$tries"); do
    port_open "$1" && return 0
    sleep 1
  done
  return 1
}

# --- http ----------------------------------------------------------------
http_code() { curl -s -o /dev/null -w '%{http_code}' --max-time 5 "$@" || echo 000; }

assert_http() { # assert_http EXPECTED [curl args...] URL
  local expected="$1"; shift
  local url="${@: -1}"
  local code
  code="$(http_code "$@")"
  if [ "$code" = "$expected" ]; then ok "$expected on $url"; else fail "expected $expected on $url, got $code"; fi
}

assert_contains() { # assert_contains URL SUBSTRING
  local url="$1" needle="$2"
  if curl -s --max-time 5 "$url" | grep -qF -- "$needle"; then
    ok "$url contains '$needle'"
  else
    fail "$url does not contain '$needle'"
  fi
}

# --- gradle --------------------------------------------------------------
require_gradlew() { [ -x "$GRADLEW" ] || die "gradlew not found at $GRADLEW (run 'gradle wrapper' or check the repo)"; }

ensure_java() {
  local v
  v="$(java -version 2>&1 | head -1 | grep -oE '"([0-9]+)' | tr -d '"')"
  [ "${v:-0}" -ge 21 ] || die "JDK 21+ required (found $(java -version 2>&1 | head -1))"
}

# --- services ------------------------------------------------------------
run_service() { # run_service MODULE  (background, logs to build/run-<module>.log)
  local module="$1"
  info "starting $module ..."
  ( cd "$ROOT_DIR" && ./gradlew ":$module:bootRun" > "build/run-$module.log" 2>&1 & )
}