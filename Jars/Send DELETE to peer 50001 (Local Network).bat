@echo off
FOR /F "tokens=4 delims= " %%i in ('route print ^| find " 0.0.0.0"') do set localIp=%%i
java -jar TestApp.jar %localIp%:50001 DELETE TestFile.ogg
pause