Option Explicit 
On Error Resume Next
dim strComputer
dim wmiNS
dim wmiQuery
dim objWMIService
Dim objLocator
dim colItems
dim objItem
Dim strUsr, strPWD, strLocl, strAuth, iFLag 'connect server parameters
Dim colNamedArguments 'WshNamed object

subCheckCscript  'check to see if running in cscript
    Set colNamedArguments = WScript.Arguments.Named
    strComputer = colNamedArguments("c")
subCheckArguments

wmiNS = "\root\cimv2"
wmiQuery = "Select BytesTotalPerSec from Win32_PerfFormattedData_Tcpip_NetworkInterface"
strUsr = colNamedArguments("u") '""'Blank for current security. Domain\Username
strPWD = colNamedArguments("p") '""'Blank for current security.
strLocl = "" '"MS_409" 'US English. Can leave blank for current language
strAuth = ""'if specify domain in strUsr this must be blank
iFlag = "0" 'only two values allowed here: 0 (wait for connection) 128 (wait max two min)

Set objLocator = CreateObject("WbemScripting.SWbemLocator")
Set objWMIService = objLocator.ConnectServer(strComputer, _
     wmiNS, strUsr, strPWD, strLocl, strAuth, iFLag)
Set colItems = objWMIService.ExecQuery(wmiQuery)

For Each objItem in colItems
   Wscript.Echo funLine("Name: " & objItem.name)
   Wscript.Echo "CurrentBandwidth: " & funConvert("K",objItem.BytesTotalPerSec )

Next


' *** subs are below ***
Sub subCheckCscript
If UCase(Right(Wscript.FullName, 11)) = "WSCRIPT.EXE" Then
    Wscript.Echo "This script must be run under CScript"
    WScript.Quit
End If
end Sub

Sub subCheckArguments
If colNamedArguments.Count < 4 Then
    If colNamedArguments.Exists("?") Then 
        WScript.Echo "Uses Win32_PerfFormattedData_Tcpip_NetworkInterface to determine bandwidth" _
    &   VbCrLf & "This script can take arguments. It will analyze bandwidth of network adapter"_
    &   VbCrLf & "This is useful when you want to see the actual bandwidth of a network adapter" _
    &   VbCrLf & "Perhaps to troubleshoot cases when the adapter autodetects the wrong speed" _
    & VbCrLf & "Alternate credentials can ONLY be supplied for remote connections" _
    & VbCrLf & "Try this: cscript " & WScript.ScriptName & " [/c:yourcomputername] [/u:domainName\UserName] [/p:password]" _
    & VbCrLf & "Example: cscript " & WScript.ScriptName & " /c:london /u:nwtraders\londonAdmin /p:P@ssw0rd" _
    & VbCrLf & vbTab & " Connects to a remote machine called london in the nwtraders domain with the londonAdmin user" _
    & VbCrLf & vbTab & " account and the password of P@ssw0rd. It returns the speed of each network adapter" _
    & VbCrLf & "Example: cscript " & WScript.ScriptName _
    & VbCrLf & vbTab & " Returns the speed of each network adapter on local machine"
    WScript.Quit
    End If
WScript.Echo "checking arguments"
    If Not colNamedArguments.Exists("c") Then
        WScript.Echo "Executing on Local Machine only"
        strComputer = "localHost"
    End If 
    If Not colNamedArguments.Exists("u") Then
        WScript.Echo "Executing using current user name"
        strUsr = ""
    End If 
    If Not colNamedArguments.Exists("p") Then
        WScript.Echo "Executing using current user password"
        strPWD = ""
    End If 
End If
If colNamedArguments.Count = 0 Then
    Exit Sub
End If
End Sub

Function funConvert(strC,intIN)
Select Case strC
Case "K"
funConvert = formatNumber(intIN/1000) & " KiloBytes"
Case "M"
funConvert = formatNumber(intIN/1000000) & " MegaBytes"
Case "G"
funConvert = formatNumber(intIN/1000000000) & " GigaBytes"
End Select
End Function

Function funLine(lineOfText)
Dim numEQs, separator, i
numEQs = Len(lineOfText)
For i = 1 To numEQs
    separator= separator & "="
Next
 FunLine = VbCrLf & lineOfText & vbcrlf & separator
End Function