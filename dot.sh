#!/bin/bash

# 引数チェック
if [ -z "$1" ]; then
  echo "使い方: $0 <target_directory>"
  exit 1
fi

TARGET_DIR="$1"

# dot → png 可視化処理
find "$TARGET_DIR" -type f -name '*.dot' -print0 \
  | while IFS= read -r -d '' f; do
      echo "▶ ${f}"
      dot -Tpng "$f" -o "${f%.dot}.png"
    done

echo "完了しました！"
