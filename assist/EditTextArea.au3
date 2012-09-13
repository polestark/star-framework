#Region ;**** 参数创建于 ACNWrapper_GUI ****
#AutoIt3Wrapper_outfile=EditTextArea.exe
#AutoIt3Wrapper_Res_Fileversion=0.0.0.0
#EndRegion ;**** 参数创建于 ACNWrapper_GUI ****

#include <IE.au3>

If $CmdLine[0] < 4 Then 
	Exit
EndIf

sendKeys($CmdLine[1],$CmdLine[2],$CmdLine[3], $CmdLine[4])

Func sendKeys($ieTitle, $findBy, $nameOrId, $text)
	
	WinActive($ieTitle)
	
	;获取指定标题的IE
	$oIE = _IEAttach ($ieTitle, "Title")
	If @error Then
		Exit
	EndIf
	
	;在dialog上找到输入框
	If	StringLower($findBy) = "id" Then
		$writeObj = _IEGetObjById($oIE, $nameOrId)
	ElseIf StringLower($findBy) = "name" Then
		$writeObj = _IEGetObjByName($oIE, $nameOrId)
	EndIf
	
	If @error Then
		Exit
	EndIf
			
	;在输入框里输入条件
	_IEDocInsertText ($writeObj, $text)
EndFunc