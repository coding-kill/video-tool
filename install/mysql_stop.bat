cd %~dp0
net stop mysql33
sc delete mysql33
"%cd%\mysqld.exe" -remove mysql33
cd ..
rd /s /Q %cd%/data
echo mysql–∂‘ÿÕÍ≥…
exit
