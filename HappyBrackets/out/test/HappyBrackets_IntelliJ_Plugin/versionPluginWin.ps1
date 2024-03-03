param (
    [String]$VERSION
)

$FILE_PATH = Split-Path -Parent $MyInvocation.MyCommand.Definition
Write-Host "Current dir: $FILE_PATH"

$FILENAME = Join-Path $FILE_PATH "resources\META-INF\plugin.xml"
$TEMP_FILE = Join-Path $FILE_PATH "resources\META-INF\temp.xml"

Write-Host "File Name: $FILENAME"

Remove-Item -Path $TEMP_FILE -ErrorAction Ignore

Get-Content $FILENAME | ForEach-Object {
    if ($_ -match "\<version\>.*\<\/version\>") {
        # write our modified version
        "  <version>$VERSION</version>"
    }
    else {
        # write our original line to temp
        $_
    }
} | Set-Content $TEMP_FILE

# now replace original file with this new one
Move-Item -Path $TEMP_FILE -Destination $FILENAME -Force
Write-Host "Complete"
