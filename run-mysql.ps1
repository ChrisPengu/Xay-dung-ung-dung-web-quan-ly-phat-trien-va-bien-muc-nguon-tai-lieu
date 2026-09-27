$ErrorActionPreference = 'Stop'

$envFile = Join-Path $PSScriptRoot '.env.local'
$mavenWrapper = Join-Path $PSScriptRoot 'mvnw.cmd'

if (-not (Test-Path -LiteralPath $envFile)) {
    throw 'Không tìm thấy .env.local. Hãy tạo file này từ .env.example hoặc chạy bước thiết lập MySQL cục bộ.'
}

foreach ($line in Get-Content -LiteralPath $envFile -Encoding UTF8) {
    $trimmed = $line.Trim()
    if (-not $trimmed -or $trimmed.StartsWith('#')) {
        continue
    }

    $separator = $trimmed.IndexOf('=')
    if ($separator -lt 1) {
        throw "Dòng cấu hình không hợp lệ trong .env.local: $trimmed"
    }

    $name = $trimmed.Substring(0, $separator).Trim()
    $value = $trimmed.Substring($separator + 1)
    if ($name -notmatch '^[A-Z][A-Z0-9_]*$') {
        throw "Tên biến môi trường không hợp lệ: $name"
    }

    [Environment]::SetEnvironmentVariable($name, $value, 'Process')
}

& $mavenWrapper spring-boot:run
exit $LASTEXITCODE
