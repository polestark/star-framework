On Error Resume Next
Set WshShell = CreateObject("WScript.Shell")

'设置自动查找代理服务（内网标装通用标准）
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\AutoConfigURL","http://proxy.paic.com.cn/proxy.pac","REG_SZ"

'设置代理服务器地址为空并且删除该代理设置项
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyServer","","REG_SZ"
WshShell.RegDelete "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyServer"

'设置本地代理例外列表为空并且删除该代理设置项
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyOverride","","REG_SZ"
WshShell.RegDelete "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyOverride"

'修改是否使用代理服务器项配置为否
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\ProxyEnable",0,"REG_DWORD"

'关闭IE的弹出窗口阻止程序
WshShell.RegWrite "HKCU\Software\Microsoft\Internet Explorer\New Windows\PopupMgr","no","REG_SZ"

'设置允许活动内容在本地计算机运行
WshShell.RegWrite "HKCU\Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_LOCALMACHINE_LOCKDOWN\iexplore.exe",0,"REG_DWORD"

'设置浏览器安全级别，本地和可信站点为低，Internet为中
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\1\CurrentLevel",10000,"REG_DWORD"
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\2\CurrentLevel",10000,"REG_DWORD"
WshShell.RegWrite "HKCU\Software\Microsoft\Windows\CurrentVersion\Internet Settings\Zones\3\CurrentLevel",11000,"REG_DWORD"

Set WshShell = Nothing