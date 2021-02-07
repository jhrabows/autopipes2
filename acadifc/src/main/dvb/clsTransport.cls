VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "clsTransport"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
 ' Copyright 2005 Jan Hrabowski
 '
 '  Licensed under the Apache License, Version 2.0 (the "License");
 '  you may not use this file except in compliance with the License.
 '  You may obtain a copy of the License at
 '
 '     http://www.apache.org/licenses/LICENSE-2.0
 '
 '  Unless required by applicable law or agreed to in writing, software
 '  distributed under the License is distributed on an "AS IS" BASIS,
 '  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 '  See the License for the specific language governing permissions and
 '  limitations under the License.
  
Option Explicit
Private Const SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/"
Private Const URL_ENV_VAR = "HYDROSERVER"
Private g_soapURL As String
Private g_http As MSXML2.XMLHTTP

Public Function xmlCall(inMsg As MSXML2.DOMDocument) As MSXML2.DOMDocument
Dim sMsg As String
  Set xmlCall = New MSXML2.DOMDocument
  Call encloseEnvelope(inMsg)
  sMsg = inMsg.xml
  Call removeEnvelope(inMsg)
  Call g_http.Open("POST", g_soapURL, False)
  Call g_http.send(sMsg)
  If g_http.statusText <> "OK" Then
    MsgBox (g_http.statusText & ":" & g_http.responseText)
  Else
    Call xmlCall.loadXML(g_http.responseText)
    Call xmlCall.setProperty("SelectionLanguage", "XPath")
    Call displayError(xmlCall)
    Call removeEnvelope(xmlCall)
  End If
End Function
Private Function displayError(doc As MSXML2.DOMDocument)
Dim body As MSXML2.IXMLDOMElement
Dim content As IXMLDOMNodeList
Dim msgRoot As MSXML2.IXMLDOMElement
   Set body = doc.documentElement.lastChild
   Set content = body.childNodes
   If content.Length > 0 Then
     Set msgRoot = content.Item(0)
     If msgRoot.nodeName = "Fault" Or msgRoot.nodeName = msgRoot.prefix & ":Fault" Then
       MsgBox (msgRoot.xml)
     End If
   End If
End Function
Public Sub saveConfiguration(conf As MSXML2.DOMDocument)
Dim ret As MSXML2.DOMDocument
  Set ret = xmlCall(conf)
End Sub
Public Sub saveDrawing(dwg As MSXML2.DOMDocument)
Dim ret As MSXML2.DOMDocument
  Set ret = xmlCall(dwg)
End Sub
Public Function renderDrawing(dwg As MSXML2.DOMDocument) As MSXML2.DOMDocument
  Set renderDrawing = xmlCall(dwg)
End Function
Private Sub removeEnvelope(doc As MSXML2.DOMDocument)
Dim body As MSXML2.IXMLDOMElement
   Set body = doc.documentElement.lastChild
   Set doc.documentElement = body.firstChild
End Sub
Private Sub encloseEnvelope(doc As MSXML2.DOMDocument)
Dim bodyContent As MSXML2.IXMLDOMElement
Dim body As MSXML2.IXMLDOMElement

Set bodyContent = doc.documentElement
Set doc.documentElement = doc.createNode("ELEMENT", "env:Envelope", SOAPENV)
Set body = doc.createNode("ELEMENT", "env:Body", SOAPENV)
Call doc.documentElement.appendChild(body)
Call body.appendChild(bodyContent)

End Sub

Private Property Get url() As Variant
  url = g_soapURL
End Property

Private Property Let url(ByVal vNewValue As Variant)
   g_soapURL = vNewValue
End Property

Private Sub Class_Initialize()
  Dim envURL As Variant
  
  envURL = Environ(URL_ENV_VAR)
  If envURL = "" Then
    g_soapURL = "http://localhost:8080/autopipes/server"
  Else
    g_soapURL = envURL
  End If
  
  Set g_http = New MSXML2.XMLHTTP
End Sub
