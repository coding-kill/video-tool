cd %~dp0
cd ..
del %cd%\my.ini
rd /s /Q %cd%/data
echo ɾ�����
echo [mysqld]>> my.ini
echo # ����3306�˿�>> my.ini
echo port=3308>> my.ini
echo # ����mysql�İ�װĿ¼>> my.ini
echo basedir=%cd:\=\\%>> my.ini
echo # ����mysql���ݿ�����ݵĴ��Ŀ¼>> my.ini
echo datadir=%cd:\=\\%\\data>> my.ini
echo # �������������>> my.ini
echo max_connections=200>> my.ini
echo # ��������ʧ�ܵĴ���������Ϊ�˷�ֹ���˴Ӹ�������ͼ�������ݿ�ϵͳ>> my.ini
echo max_connect_errors=10>> my.ini
echo # �����ʹ�õ��ַ���Ĭ��ΪUTF8>> my.ini
echo character-set-server=utf8>> my.ini
echo # �����±�ʱ��ʹ�õ�Ĭ�ϴ洢����>> my.ini
echo default-storage-engine=INNODB>> my.ini
echo [mysql]>> my.ini
echo # ����mysql�ͻ���Ĭ���ַ���>> my.ini
echo default-character-set=utf8>> my.ini
echo [client]>> my.ini
echo # ����mysql�ͻ������ӷ����ʱĬ��ʹ�õĶ˿�>> my.ini
echo port=3308>> my.ini
echo default-character-set=utf8>> my.ini
echo my.ini���ɳɹ�
set inipath=%cd%\my.ini
cd bin
"%cd%\mysqld.exe" -remove mysql33
"%cd%\mysqld.exe" -install mysql33 --defaults-file="%inipath%"
"%cd%\mysqld.exe" --initialize-insecure --console
net start mysql33
sc config mysql33 start=auto
net stop mysql33
net start mysql33
echo ��װ���
"%cd%\mysqladmin.exe" -u root password root -P3308
echo �޸��������
cd ..
"%cd%\bin\mysql.exe" -uroot -proot -P3308< "%cd:\=\\%\\initsql\\myInitSql.sql"
echo ���ݿ��ʼ�����
echo ���
exit