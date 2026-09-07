# load_mes_seed.ps1
# 将 MES 源数据种子载入本地 PostgreSQL，使 QMS 不连 MES 也能展示完整数据。
# 用法:  $env:QMS_DB_PASSWORD='xxx'; .\load_mes_seed.ps1 [-Reset] [-HostName localhost] [-Port 5432] [-Database qms] [-User qms]
param(
    [string]$HostName = 'localhost',
    [int]$Port = 5432,
    [string]$Database = 'qms',
    [string]$User = 'qms',
    [string]$Password = $env:QMS_DB_PASSWORD,
    [switch]$Reset
)

$ErrorActionPreference = 'Stop'

if (-not $Password) {
    $Password = Read-Host -Prompt 'PostgreSQL 密码' -AsSecureString
    $Password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password))
}

$env:PGHOST = $HostName
$env:PGPORT = $Port
$env:PGDATABASE = $Database
$env:PGUSER = $User
$env:PGPASSWORD = $Password

$seedDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$files = @(
    'mes_schema.sql',
    'finished_goods_inspection_202608071045.sql.gz',
    'critical_material_binding_202608071045.sql.gz',
    'material_inspection_202608071045.sql.gz'
)

function Load-File($relPath) {
    $p = Join-Path $seedDir $relPath
    if ($relPath -like '*.gz') {
        $tmp = Join-Path $env:TEMP ([System.IO.Path]::GetRandomFileName() + '.sql')
        python -c "import gzip; open(r'$tmp','wb').write(gzip.open(r'$p','rb').read())"
        try { psql -v ON_ERROR_STOP=1 -f $tmp }
        finally { Remove-Item $tmp -ErrorAction SilentlyContinue }
    } else {
        psql -v ON_ERROR_STOP=1 -f $p
    }
}

if ($Reset) {
    Write-Host '重建 qms 种子 schema (DROP SCHEMA qms CASCADE) ...'
    psql -v ON_ERROR_STOP=1 -c "DROP SCHEMA IF EXISTS qms CASCADE;"
}

foreach ($f in $files) {
    Write-Host "载入 $f ..."
    Load-File $f
}

Write-Host '完成。启动后端后, MesDataSyncJob 会自动从 qms.* 同步生成 ops 供应商/异常/批次数据。'
