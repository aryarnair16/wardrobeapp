Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  👗 WearNet - Digital Wardrobe & Peer Sharing Platform" -ForegroundColor Yellow
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting Embedded Java Web Application on http://localhost:8080..." -ForegroundColor Green

$java = "C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
if (-not (Test-Path $java)) {
    $java = "java"
}

$cp = "target\classes;C:\Users\aryar\.m2\repository\org\xerial\sqlite-jdbc\3.46.1.1\sqlite-jdbc-3.46.1.1.jar"

& $java -cp $cp com.mycompany.wardrobeapp.Wardrobeapp
