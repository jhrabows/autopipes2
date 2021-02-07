VERSION 5.00
Begin {C62A69F0-16DC-11CE-9E98-00AA00574A4F} frmPipeUtils 
   Caption         =   "Pipe Utilities"
   ClientHeight    =   3900
   ClientLeft      =   45
   ClientTop       =   330
   ClientWidth     =   6120
   OleObjectBlob   =   "frmPipeUtils.frx":0000
   StartUpPosition =   1  'CenterOwner
End
Attribute VB_Name = "frmPipeUtils"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
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

' Name of command tab
Private Const strCmdPage = "pgCommands"
Private Sub loadOptions()
Dim sVal As String
Dim dVal As Double
Dim nVal As Integer
Dim bVal As Boolean
   sVal = modUtils.goXmlIfc.getBrPrefix
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setBrPrefix(txtBrPrefix.text)
'   Else
'     txtBrPrefix.text = sVal
'   End If
'   nVal = modUtils.goXmlIfc.getBrStart
'   If nVal < 0 Then
'     Call modUtils.goXmlIfc.setBrStart(txtBrStart.text)
'   Else
'     txtBrStart.text = nVal
'   End If
'   sVal = modUtils.goXmlIfc.getMainPrefix
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setMainPrefix(txtMainPrefix.text)
'   Else
'     txtMainPrefix.text = sVal
'   End If
'   nVal = modUtils.goXmlIfc.getMainStart
'   If nVal < 0 Then
'     Call modUtils.goXmlIfc.setMainStart(txtMainStart.text)
'   Else
'     txtMainStart.text = nVal
'   End If
   sVal = modUtils.goXmlIfc.getErrorColor
   If sVal = "" Then
     Call modUtils.goXmlIfc.setErrorColor(lblErrColorSel.Caption)
   Else
     lblErrColorSel.Caption = sVal
     lblErrColorSel.BackColor = vbColorCode(sVal)
   End If
   
   bVal = modUtils.goXmlIfc.getWeldIfEq
   Call modUtils.goXmlIfc.setWeldIfEq(bVal)
   chkWeldIfEq.value = bVal
   
   bVal = modUtils.goXmlIfc.getABCO
   Call modUtils.goXmlIfc.setABCO(bVal)
   chkABCO.value = bVal

   
'   sVal = modUtils.goXmlIfc.getMainType
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setMainType(lblMainSel.Caption)
'   Else
'     lblMainSel.Caption = sVal
'   End If
'   sVal = modUtils.goXmlIfc.getVendor
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setVendor(lblVenSel.Caption)
'   Else
'     lblVenSel.Caption = sVal
'   End If
   sVal = modUtils.goXmlIfc.getMainCut
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setMainCut(txtBreakMain.text)
'   Else
'     txtBreakMain.text = sVal
'   End If
  ' dVal = modUtils.goXmlIfc.getLinearDelta
  ' If dVal < 0 Then
  '   Call modUtils.goXmlIfc.setLinearDelta(txtLinDelta.text)
  ' Else
  '   txtLinDelta.text = dVal
  ' End If
  ' dVal = modUtils.goXmlIfc.getAngularDelta
  ' If dVal < 0 Then
  '   Call modUtils.goXmlIfc.setAngularDelta(txtAngDelta.text)
  ' Else
  '   txtAngDelta.text = dVal
  ' End If
   dVal = modUtils.goXmlIfc.getShortPipe
'   If dVal < 0 Then
'     Call modUtils.goXmlIfc.setShortPipe(txtShort.text)
'   Else
'     txtShort.text = dVal
'   End If
   dVal = modUtils.goXmlIfc.getLongPipe
'   If dVal < 0 Then
'     Call modUtils.goXmlIfc.setLongPipe(txtLong.text)
'   Else
'     txtLong.text = dVal
'   End If
   sVal = modUtils.goXmlIfc.getBranchCut
'   If sVal = "" Then
'     Call modUtils.goXmlIfc.setBranchCut(txtBreakBranch.text)
'   Else
'     txtBreakBranch.text = sVal
'   End If
   dVal = modUtils.goXmlIfc.getMainCutSpace
'   If dVal < 0 Then
'     Call modUtils.goXmlIfc.setMainCutSpace(txtSpace.text)
'   Else
'     txtSpace.text = dVal
'   End If
End Sub
Private Sub loadLayers()
    Dim nCnt As Integer
    Dim I As Integer
    Dim strLayer As String

    nCnt = ThisDrawing.Application.ActiveDocument.Layers.Count
    For I = 0 To nCnt - 1
      strLayer = ThisDrawing.Application.ActiveDocument.Layers.Item(I).name
        Call lstLayers.AddItem(strLayer, I)
        lstLayers.Column(1, I) = modUtils.goXmlIfc.GetLayerType(strLayer)
    Next
End Sub
Private Sub loadDiameters()
Dim I As Integer
  For I = 0 To 11
    Call lstDiameters.AddItem(I + 1, I)
    lstDiameters.Column(1, I) = ""
  Next
End Sub

Private Sub chkSaveDefaults_Click()
  Dim bVal As Boolean
  bVal = chkSaveDefaults.value
  modUtils.goXmlIfc.setSaveWithDefaults (bVal)
End Sub
Private Sub cmdExtract_Click()
  modUtils.gsCommand = modUtils.cmdExtract
  Me.Hide
End Sub

Private Sub cmdImport_Click()
Dim request As MSXML2.DOMDocument
Dim reply As MSXML2.DOMDocument
Dim idx As Integer
Dim id As Long
   idx = lstStatus.ListIndex
   If idx < 0 Then
      MsgBox ("Select area")
      Exit Sub
   End If
   id = lstStatus.Column(0, idx)
   Set request = modUtils.goXmlIfc.createRenderingReq(id)
   If chkShowBrLabels.value Then
     Call modUtils.goXmlIfc.renderBranchLabels(request)
   End If
   If chkShowMainLabels.value Then
     Call modUtils.goXmlIfc.renderMainLabels(request)
   End If
   If chkShowBrCuts.value Then
     Call modUtils.goXmlIfc.renderBranchCuts(request)
   End If
   If chkShowMainCuts.value Then
     Call modUtils.goXmlIfc.renderMainCuts(request)
   End If
   If chkShowMainDiam.value Then
     Call modUtils.goXmlIfc.renderMainDiam(request)
   End If
   If chkShowBrDiam.value Then
     Call modUtils.goXmlIfc.renderBranchDiam(request)
   End If
   If chkShowMainSpan.value Then
     Call modUtils.goXmlIfc.renderMainSpan(request)
   End If
   If chkShowBrSpan.value Then
     Call modUtils.goXmlIfc.renderBranchSpan(request)
   End If
   Set reply = modUtils.goTransport.renderDrawing(request)
'   Call modUtils.goXmlIfc.importDoc(reply)
   Call modUtils.goXmlIfc.renderStatus(reply)
   ThisDrawing.Application.Update
End Sub

Private Sub cmdErrors_Click()
Dim idx As Integer
Dim id As Integer
Dim request As MSXML2.DOMDocument
Dim reply As MSXML2.DOMDocument

  idx = lstStatus.ListIndex
  If idx >= 0 Then
    id = lstStatus.Column(0, idx)
    If chkLocateRaiser.value Then
      If modUtils.goXmlIfc.raiserCount(id) > 0 Then
        Call modUtils.goXmlIfc.importRaisers(id)
      Else
        Call modUtils.goXmlIfc.colorEdges(id)
      End If
    End If
    If chkLocateCenter.value Then
      Call modUtils.goXmlIfc.importCenters(id)
    End If

   End If

   If chkShowHeads.value Then
     Set request = modUtils.goXmlIfc.createRenderingReq
     Call modUtils.goXmlIfc.renderHeads(request)
     Set reply = modUtils.goTransport.renderDrawing(request)
     Call modUtils.goXmlIfc.importDoc(reply)
   End If
  
   If chkShowErrors.value Then
     Call modUtils.goXmlIfc.importEntityErrors
   End If
   ThisDrawing.Application.Update
End Sub

Private Function vbColorCode(colorTxt As String) As Long
Dim ret As Long
If colorTxt = "Magenta" Then
  ret = vbMagenta
ElseIf colorTxt = "Cyan" Then
  ret = vbCyan
Else
  ret = vbWhite
End If
vbColorCode = ret
End Function



Private Sub lblErrColorSel_DblClick(ByVal Cancel As MSForms.ReturnBoolean)
Dim sVal As String
If lblErrColorSel.Caption = "Magenta" Then
  sVal = "Cyan"
Else
  sVal = "Magenta"
End If
lblErrColorSel.Caption = sVal
Call modUtils.goXmlIfc.setErrorColor(sVal)
lblErrColorSel.BackColor = vbColorCode(sVal)
End Sub

Private Sub lblMainSel_DblClick(ByVal Cancel As MSForms.ReturnBoolean)
Dim sVal As String
Dim nListIndex As Integer
Dim sLayer As String
Dim sType As String
Dim sVend As String
Dim nCurDigits As Integer
Dim nMinDigits As Integer
nListIndex = lstLayers.ListIndex
If nListIndex >= 0 Then
    sLayer = lstLayers.Column(0, nListIndex)
    sType = lstLayers.Column(1, nListIndex)
    If sType = "Main" Then
      sVal = modUtils.goXmlIfc.nextMainSubType(lblMainSel.Caption)
      sVend = modUtils.goXmlIfc.nextVendor(sVal, "")
      Call modUtils.goXmlIfc.setMainType(sLayer, sVal)
      Call modUtils.goXmlIfc.setVendor(sLayer, sVend)
      lblMainSel.Caption = sVal
      lblVenSel.Caption = sVend
      ' make sure that the diameter is at least 2" when switching to grooved main
      nCurDigits = modUtils.goXmlIfc.DiameterDisplayToDigits(lblMainDiam.Caption)
      nMinDigits = modUtils.goXmlIfc.firstMainDiameterDigits(sVal)
      If (nCurDigits < nMinDigits) Then
        Call modUtils.goXmlIfc.setMainDiameter(sLayer, nMinDigits)
        lblMainDiam.Caption = modUtils.goXmlIfc.DiameterDigitsToDisplay(nMinDigits)
      End If
    End If
End If
End Sub
Private Sub lblVenSel_DblClick(ByVal Cancel As MSForms.ReturnBoolean)
Dim sVal As String
Dim nListIndex As Integer
Dim sLayer As String
Dim sSubType As String

nListIndex = lstLayers.ListIndex
sSubType = lblMainSel.Caption
If nListIndex >= 0 Then
 sLayer = lstLayers.Column(0, nListIndex)
 sVal = modUtils.goXmlIfc.nextVendor(sSubType, lblVenSel.Caption)
 Call modUtils.goXmlIfc.setVendor(sLayer, sVal)
 lblVenSel.Caption = sVal
End If
End Sub

Private Sub lstLayers_Click()
' Selecting new layer
Dim nSel As Integer
Dim sLayer As String
Dim nDiam As Integer
Dim sDiam As String
Dim nHeadCount As Integer
Dim sMainType As String
Dim sMainVendor As String
 nSel = lstLayers.ListIndex
 sLayer = lstLayers.Column(0, nSel)
 sMainType = modUtils.goXmlIfc.getMainType(sLayer)
 sMainVendor = modUtils.goXmlIfc.getVendor(sLayer)
 For nHeadCount = -1 To 12
    If nHeadCount = -1 Then
      nDiam = modUtils.goXmlIfc.getHoleDiameter(sLayer)
    ElseIf nHeadCount = 0 Then
      nDiam = modUtils.goXmlIfc.getMainDiameter(sLayer)
    Else
      nDiam = modUtils.goXmlIfc.GetLayerDiameter(sLayer, nHeadCount)
    End If
    If nDiam = 0 Then
     sDiam = ""
    Else
     sDiam = modUtils.goXmlIfc.DiameterDigitsToDisplay(nDiam)
    End If
    If nHeadCount = 0 Then
        lblMainDiam.Caption = sDiam
        lblMainSel.Caption = sMainType
        lblVenSel.Caption = sMainVendor
    ElseIf nHeadCount = -1 Then
        lblHoleDiam.Caption = sDiam
    Else
        lstDiameters.Column(1, nHeadCount - 1) = sDiam
    End If
Next
End Sub

Private Sub lstLayers_DblClick(ByVal Cancel As MSForms.ReturnBoolean)
Dim nSel As Integer
Dim sVal As String
Dim sLayer As String
Dim sVendor As String
Dim sSubType As String
 sSubType = lblMainSel.Caption
 sVendor = lblVenSel.Caption
 nSel = lstLayers.ListIndex
 sVal = lstLayers.Column(1, nSel)
 sLayer = lstLayers.Column(0, nSel)
 sVal = modUtils.goXmlIfc.NextLayerType(sVal)
 lstLayers.Column(1, nSel) = sVal
 Call modUtils.goXmlIfc.SetLayerAttributes(sLayer, sVal, sSubType, sVendor)
End Sub
Private Function spinDiameter(sDiam As String, nDir As Integer, nFromIdx As Integer, nToIdx As Integer) As Integer
Dim nDiam As Integer
Dim nDiamIdx As Integer
        If sDiam <> "" Then
            nDiam = modUtils.goXmlIfc.DiameterDisplayToDigits(sDiam)
            nDiamIdx = modUtils.goXmlIfc.FindDiameterIndex(nDiam)
            nDiamIdx = nDiamIdx + nDir
            If nDiamIdx < nFromIdx Then
              nDiamIdx = nToIdx
            ElseIf nDiamIdx > nToIdx Then
              nDiamIdx = nFromIdx
            End If
        Else
            nDiamIdx = nFromIdx
        End If
        spinDiameter = modUtils.goXmlIfc.getDiameterDigits(nDiamIdx)
End Function
Private Function spinBranchDiameterDisplay(sDiam As String, nDir As Integer, sFrom As String, sTo As String) As Integer
Dim nDiamFrom As Integer
Dim nDiamFromIdx As Integer
Dim nDiamTo As Integer
Dim nDiamToIdx As Integer
        If sFrom <> "" Then
            nDiamFrom = modUtils.goXmlIfc.DiameterDisplayToDigits(sFrom)
            nDiamFromIdx = modUtils.goXmlIfc.FindDiameterIndex(nDiamFrom)
        Else
          nDiamFromIdx = 0
        End If
        If sTo <> "" Then
            nDiamTo = modUtils.goXmlIfc.DiameterDisplayToDigits(sTo)
        Else
            nDiamTo = modUtils.goXmlIfc.getDiameterDigits(-1)
        End If
        nDiamToIdx = modUtils.goXmlIfc.FindDiameterIndex(nDiamTo)
        spinBranchDiameterDisplay = spinDiameter(sDiam, nDir, nDiamFromIdx, nDiamToIdx)
End Function
Private Function spinMainDiameterDisplay(sDiam As String, sType As String, nDir As Integer) As Integer
Dim nDiamFrom As Integer
Dim nDiamFromIdx As Integer
Dim nDiamTo As Integer
Dim nDiamToIdx As Integer
        nDiamFrom = modUtils.goXmlIfc.firstMainDiameterDigits(sType)
        nDiamFromIdx = modUtils.goXmlIfc.FindDiameterIndex(nDiamFrom)
        nDiamTo = modUtils.goXmlIfc.getDiameterDigits(-1)
        nDiamToIdx = modUtils.goXmlIfc.FindDiameterIndex(nDiamTo)
        spinMainDiameterDisplay = spinDiameter(sDiam, nDir, nDiamFromIdx, nDiamToIdx)
End Function
Private Function spinHoleDiameterDisplay(sMainDiam As String, sHoleDiam As String, nDir As Integer) As Integer
Dim nMainDiam As Integer
Dim nDiamToIdx As Integer
Dim nRet As Integer
    nMainDiam = modUtils.goXmlIfc.DiameterDisplayToDigits(sMainDiam)
    nDiamToIdx = modUtils.goXmlIfc.FindDiameterIndex(nMainDiam)
    nRet = spinDiameter(sHoleDiam, nDir, 0, nDiamToIdx)
    If nRet = nMainDiam Then
      nRet = 0
    End If
    spinHoleDiameterDisplay = nRet
End Function
Private Function findAdjacentDiam(nIndex As Integer, nDir As Integer) As String
Dim I As Integer
findAdjacentDiam = ""
If nDir > 0 Then
  For I = nIndex + 1 To lstDiameters.ListCount - 1
    If lstDiameters.Column(1, I) <> "" Then
     findAdjacentDiam = lstDiameters.Column(1, I)
     Exit For
    End If
  Next I
Else
  For I = nIndex - 1 To 0 Step -1
    If lstDiameters.Column(1, I) <> "" Then
     findAdjacentDiam = lstDiameters.Column(1, I)
     Exit For
    End If
  Next I
End If
End Function
Private Sub spinHoleDiam(nDir As Integer)
Dim sMainDiam As String
Dim sHoleDiam As String
Dim nDiam As Integer
Dim nListIndex As Integer
Dim sLayer As String
Dim sType As String
  ' Find which layer is selected
  nListIndex = lstLayers.ListIndex
  If nListIndex >= 0 Then
    ' get name and type
    sLayer = lstLayers.Column(0, nListIndex)
    sType = lstLayers.Column(1, nListIndex)
    If sType = "Main" Then
        sMainDiam = lblMainDiam.Caption
        If sMainDiam <> "" Then
          sHoleDiam = lblHoleDiam.Caption
          nDiam = spinHoleDiameterDisplay(sMainDiam, sHoleDiam, nDir)
          If nDiam > 0 Then
            sHoleDiam = modUtils.goXmlIfc.DiameterDigitsToDisplay(nDiam)
          Else
            sHoleDiam = ""
          End If
          lblHoleDiam.Caption = sHoleDiam
          'Call modUtils.goXmlIfc.SetLayerDiameter(sLayer, -1, nDiam)
          Call modUtils.goXmlIfc.setHoleDiameter(sLayer, nDiam)
        End If
    End If
  End If
End Sub
Private Sub spinMainDiam(nDir As Integer)
Dim sDiam As String
Dim sDiamPrev As String
Dim sDiamNext As String
Dim nDiam As Integer
Dim nDiamIdx As Integer
Dim nListIndex As Integer
Dim nSubListIndex As Integer
Dim sLayer As String
Dim sType As String
  ' Find which layer is selected
  nListIndex = lstLayers.ListIndex
  If nListIndex >= 0 Then
    ' get name and type
    sLayer = lstLayers.Column(0, nListIndex)
    sType = lstLayers.Column(1, nListIndex)
    If sType = "Branch" Then
        ' Find head cound selected
        nSubListIndex = lstDiameters.ListIndex
        If nSubListIndex >= 0 Then
            ' Force them to assign diameter to 1st head count
            If nSubListIndex = 0 Or lstDiameters.Column(1, 0) <> "" Then
                sDiamPrev = findAdjacentDiam(nSubListIndex, -1)
                sDiamNext = findAdjacentDiam(nSubListIndex, 1)
                sDiam = lstDiameters.Column(1, nSubListIndex)
                nDiam = spinBranchDiameterDisplay(sDiam, nDir, sDiamPrev, sDiamNext)
                sDiam = modUtils.goXmlIfc.DiameterDigitsToDisplay(nDiam)
                lstDiameters.Column(1, nSubListIndex) = sDiam
                Call modUtils.goXmlIfc.SetLayerDiameter(sLayer, nSubListIndex + 1, nDiam)
            End If
        End If
    ElseIf sType = "Main" Then
    'find type
        sType = lblMainSel.Caption
        sDiam = lblMainDiam.Caption
        nDiam = spinMainDiameterDisplay(sDiam, sType, nDir)
        sDiam = modUtils.goXmlIfc.DiameterDigitsToDisplay(nDiam)
        lblMainDiam.Caption = sDiam
        'Call modUtils.goXmlIfc.SetLayerDiameter(sLayer, 0, nDiam)
        Call modUtils.goXmlIfc.setMainDiameter(sLayer, nDiam)
        ' always reset hole diameter if main diameter changes
        lblHoleDiam.Caption = ""
        'Call modUtils.goXmlIfc.SetLayerDiameter(sLayer, -1, 0)
        Call modUtils.goXmlIfc.setHoleDiameter(sLayer, 0)
    End If
  End If
End Sub
Private Sub mpgPipes_Change()
Dim conf As MSXML2.DOMDocument
Dim status As MSXML2.DOMDocument
  If mpgPipes.SelectedItem.name = strCmdPage Then
       Set conf = modUtils.goXmlIfc.configuration
        'modUtils.goXmlIfc.extractconfiguration
       Set status = modUtils.goTransport.xmlCall(conf)
       modUtils.goXmlIfc.status = status
       Call modUtils.goXmlIfc.loadStatus(lstStatus, chkShowErrors)
  End If
End Sub
Private Sub spnHoleDiam_SpinDown()
   spinHoleDiam (-1)
End Sub
Private Sub spnHoleDiam_SpinUp()
   spinHoleDiam (1)
End Sub
Private Sub spnMainDiam_SpinDown()
   spinMainDiam (-1)
End Sub
Private Sub spnMainDiam_SpinUp()
   spinMainDiam (1)
End Sub
'Private Sub txtAngDelta_Change()
' If IsNumeric(txtAngDelta.text) Then
'    Call modUtils.goXmlIfc.setAngularDelta(txtAngDelta.text)
' End If
'End Sub
Private Sub txtBreakBranch_Change()
 Dim firstItem As Double
 If modUtils.goXmlIfc.IsSequenceNumeric(txtBreakBranch.text) Then
    firstItem = modUtils.goXmlIfc.firstToken(LTrim(txtBreakBranch.text))(0)
    If firstItem < txtLong Then
      Call modUtils.goXmlIfc.setBranchCut(txtBreakBranch.text)
    End If
 End If
End Sub
Private Sub txtBreakMain_Change()
 Dim firstItem As Double
 If Len(LTrim(txtBreakMain.text)) > 0 And modUtils.goXmlIfc.IsSequenceNumeric(txtBreakMain.text) Then
    firstItem = modUtils.goXmlIfc.firstToken(LTrim(txtBreakMain.text))(0)
    If firstItem <= txtLong Then
      Call modUtils.goXmlIfc.setMainCut(txtBreakMain.text)
    End If
 End If
End Sub

Private Sub txtBrPrefix_Change()
   Call modUtils.goXmlIfc.setBrPrefix(txtBrPrefix.text)
End Sub

Private Sub txtBrStart_Change()
  If IsNumeric(txtBrStart.text) Then
    Call modUtils.goXmlIfc.setBrStart(txtBrStart.text \ 1)
  End If
End Sub

'Private Sub txtLinDelta_Change()
' If IsNumeric(txtLinDelta.text) Then
'    Call modUtils.goXmlIfc.setLinearDelta(txtLinDelta.text)
' End If
'End Sub
Private Sub txtLong_Change()
Dim firstBrItem As Double
Dim firstMainItem As Double
 If IsNumeric(txtLong.text) Then
    firstBrItem = modUtils.goXmlIfc.firstToken(LTrim(txtBreakBranch.text))(0)
    firstMainItem = modUtils.goXmlIfc.firstToken(LTrim(txtBreakMain.text))(0)
    If txtLong.text > firstBrItem And txtLong.text > firstMainItem Then
      Call modUtils.goXmlIfc.setLongPipe(txtLong.text)
    End If
 End If
End Sub

Private Sub txtMainPrefix_Change()
   Call modUtils.goXmlIfc.setMainPrefix(txtMainPrefix.text)
End Sub
Private Sub txtMainStart_Change()
  If IsNumeric(txtMainStart.text) Then
    Call modUtils.goXmlIfc.setMainStart(txtMainStart.text \ 1)
  End If
End Sub

Private Sub txtShort_Change()
 If IsNumeric(txtShort.text) Then
    Call modUtils.goXmlIfc.setShortPipe(txtShort.text)
 End If
End Sub
Private Sub txtSpace_Change()
 If IsNumeric(txtSpace.text) Then
    Call modUtils.goXmlIfc.setMainCutSpace(txtSpace.text)
 End If
End Sub
Private Sub UserForm_Activate()
   Call modUtils.goXmlIfc.loadStatus(lstStatus, chkShowErrors)
End Sub
Private Sub UserForm_Initialize()
 Call loadLayers
 Call loadDiameters
 Call loadOptions
End Sub
Private Sub chkABCO_Click()
  Dim bVal As Boolean
  bVal = chkABCO.value
  modUtils.goXmlIfc.setABCO (bVal)
End Sub
Private Sub chkWeldIfEq_Click()
  Dim bVal As Boolean
  bVal = chkWeldIfEq.value
  modUtils.goXmlIfc.setWeldIfEq (bVal)
End Sub
