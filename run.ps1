echo " Hello deploying locally to 4 servers"

$src = "C:\users\david\documents\projects\dau_core_kt\out\artifacts\dau_core_kt_jar\dau_core_kt.jar"
$dst = "C:\users\david\documents\projects\dau_core_kt\test_servers\"
$zip = "C:\users\david\documents\projects\dau_core_kt\"

Copy-Item -Path $src -Destination $dst"server1"
Copy-Item -Path $src -Destination $dst"server2"
Copy-Item -Path $src -Destination $dst"server3"
Copy-Item -Path $src -Destination $dst"server4"

Compress-Archive -LiteralPath $dst"server1" -DestinationPath $zip"dau_core_kt.zip"
