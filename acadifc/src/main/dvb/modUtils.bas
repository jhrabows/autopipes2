Attribute VB_Name = "modUtils"
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

Public goXmlIfc As Object
Public goTransport As Object
Public gsCommand As String
Public cmdExtract As String
Public Sub pipeUtils()
  Dim status As MSXML2.DOMDocument
  Call init
  If goXmlIfc.validateAcad = False Then
    Exit Sub
  End If
  Do
   gsCommand = ""
   frmPipeUtils.Show
   If gsCommand = cmdExtract Then
    If goXmlIfc.ExtractSelection Then
      Set status = goTransport.xmlCall(goXmlIfc.drawing)
      Call goXmlIfc.renderStatus(status)
      ThisDrawing.Application.Update
    End If
   Else
    Exit Do
   End If
  Loop
  Set goXmlIfc = Nothing
  Set goTransport = Nothing
End Sub
Private Sub init()
 Set goXmlIfc = CreateXmlIfc
 Set goTransport = CreateTransport
 cmdExtract = "EXTRACT"
End Sub
