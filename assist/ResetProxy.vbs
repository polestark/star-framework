On Error Resume Next
Set WshShell = CreateObject("WScript.Shell")

'�����Զ����Ҵ������������װͨ�ñ�׼��
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\AutoConfigURL","http://proxy.paic.com.cn/proxy.pac","REG_SZ"

'���ô����������ַΪ�ղ���ɾ���ô���������
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyServer","","REG_SZ"
WshShell.RegDelete "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyServer"

'���ñ��ش��������б�Ϊ�ղ���ɾ���ô���������
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyOverride","","REG_SZ"
WshShell.RegDelete "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyOverride"

'�޸��Ƿ�ʹ�ô��������������Ϊ��
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyEnable",0,"REG_DWORD"

'�ر�IE�ĵ���������ֹ����
WshShell.RegWrite "HKCU\Software\Microsoft\Internet Explorer\New Windows\PopupMgr","no","REG_SZ"

'�������������ڱ��ؼ��������
WshShell.RegWrite "HKCU\Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_LOCALMACHINE_LOCKDOWN\iexplore.exe",0,"REG_DWORD"

'�����������ȫ���𣬱��غͿ���վ��Ϊ�ͣ�InternetΪ��
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\1\CurrentLevel",10000,"REG_DWORD"
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\2\CurrentLevel",10000,"REG_DWORD"
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\3\CurrentLevel",11000,"REG_DWORD"

Set WshShell = Nothing