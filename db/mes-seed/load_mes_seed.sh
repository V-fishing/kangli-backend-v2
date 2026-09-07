#!/usr/bin/env bash
# load_mes_seed.sh - 将 MES 源数据种子载入 PostgreSQL (无 MES 也能展示完整数据)
# 用法: QMS_DB_PASSWORD='xxx' ./load_mes_seed.sh [--reset]
set -euo pipefail

HOST="${QMS_DB_HOST:-localhost}"
PORT="${QMS_DB_PORT:-5432}"
DB="${QMS_DB_NAME:-qms}"
USER="${QMS_DB_USER:-qms}"
export PGPASSWORD="${QMS_DB_PASSWORD:-}"

DIR="$(cd "$(dirname "$0")" && pwd)"
FILES=(mes_schema.sql finished_goods_inspection_202608071045.sql.gz critical_material_binding_202608071045.sql.gz material_inspection_202608071045.sql.gz)

load_file() {
    local f="$1"
    if [[ "$f" == *.gz ]]; then
        gzip -dc "$DIR/$f" | psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DB" -v ON_ERROR_STOP=1
    else
        psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DB" -v ON_ERROR_STOP=1 -f "$DIR/$f"
    fi
}

if [ "${1:-}" = "--reset" ]; then
    echo "重建 qms 种子 schema (DROP SCHEMA qms CASCADE) ..."
    psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DB" -v ON_ERROR_STOP=1 \
        -c "DROP SCHEMA IF EXISTS qms CASCADE;"
fi

for f in "${FILES[@]}"; do
    echo "载入 $f ..."
    load_file "$f"
done

echo "完成。启动后端后 MesDataSyncJob 会自动从 qms.* 同步生成 ops 数据。"
