param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MySqlArguments
)

$ErrorActionPreference = 'Stop'
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
$credentialFile = Join-Path $env:LOCALAPPDATA 'DocuCatalog\mysql-root.credential.xml'

if (-not (Test-Path -LiteralPath $mysql)) {
    throw 'Không tìm thấy MySQL client 8.4.'
}
if (-not (Test-Path -LiteralPath $credentialFile)) {
    throw 'Không tìm thấy credential quản trị MySQL của DocuCatalog.'
}

$credential = Import-Clixml -LiteralPath $credentialFile
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($credential.Password)
try {
    $env:MYSQL_PWD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    & $mysql '--no-defaults' '--protocol=TCP' '--host=localhost' '--port=3306' '--user=root' @MySqlArguments
    exit $LASTEXITCODE
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}
