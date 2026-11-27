#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PHP_DIR="$(cd "$HERE/.." && pwd)"
REPO="$(cd "$PHP_DIR/.." && pwd)"
OUT="$PHP_DIR/src/Generated"
PLUGIN="$PHP_DIR/vendor/bin/protoc-gen-omnibuf-php"
if [[ ! -x "$PLUGIN" ]]; then
  echo "missing $PLUGIN: run composer install in $PHP_DIR first" >&2
  exit 1
fi
mkdir -p "$OUT"
protoc \
  --plugin="protoc-gen-omnibuf-php=$PLUGIN" \
  --omnibuf-php_out="$OUT" \
  --omnibuf-php_opt='namespace_prefix=OmniBuf' \
  --proto_path="$REPO/schemas/v2/protobuf" \
  "$REPO/schemas/v2/protobuf/benchmark_v2.proto"
# Keep only the 13 suite classes matching src/Generated/Benchmark/V2/.
rm -f "$OUT/OmniBuf/Benchmark/V2"/Opaque*.php \
  "$OUT/OmniBuf/Benchmark/V2"/{Table,BatchTable,NestedRow,NestedMeta,NestedItem,BatchNestedRow,Signal,SignalLeg,BatchSignal}.php
echo "wrote PHP OmniBuf classes under $OUT/OmniBuf"
