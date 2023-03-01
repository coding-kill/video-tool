cd %~dp0
cd ..
del %cd%\my.ini
rd /s /Q %cd%/data
echo 删除完成
echo [mysqld]>> my.ini
echo # 设置3306端口>> my.ini
echo port=3308>> my.ini
echo # 设置mysql的安装目录>> my.ini
echo basedir=%cd:\=\\%>> my.ini
echo # 设置mysql数据库的数据的存放目录>> my.ini
echo datadir=%cd:\=\\%\\data>> my.ini
echo # 允许最大连接数>> my.ini
echo max_connections=200>> my.ini
echo # 允许连接失败的次数。这是为了防止有人从该主机试图攻击数据库系统>> my.ini
echo max_connect_errors=10>> my.ini
echo # 服务端使用的字符集默认为UTF8>> my.ini
echo character-set-server=utf8>> my.ini
echo # 创建新表时将使用的默认存储引擎>> my.ini
echo default-storage-engine=INNODB>> my.ini
echo [mysql]>> my.ini
echo # 设置mysql客户端默认字符集>> my.ini
echo default-character-set=utf8>> my.ini
echo [client]>> my.ini
echo # 设置mysql客户端连接服务端时默认使用的端口>> my.ini
echo port=3308>> my.ini
echo default-character-set=utf8>> my.ini
echo my.ini生成成功
set inipath=%cd%\my.ini
cd bin
"%cd%\mysqld.exe" -remove mysql33
"%cd%\mysqld.exe" -install mysql33 --defaults-file="%inipath%"
"%cd%\mysqld.exe" --initialize-insecure --console
net start mysql33
sc config mysql33 start=auto
net stop mysql33
net start mysql33
echo 安装完毕
"%cd%\mysqladmin.exe" -u root password root -P3308
echo 修改密码完毕
cd ..
"%cd%\bin\mysql.exe" -uroot -proot -P3308< "%cd:\=\\%\\initsql\\myInitSql.sql"
echo 数据库初始化完成
echo 完成
exit