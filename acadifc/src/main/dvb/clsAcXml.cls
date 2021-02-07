VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "clsAcXml"
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
'common schema
Private Const DWG_NS_PREFIX = "dwg"
Private Const DWG_NAMESPACE = "http://dwg.autopipes.org"

'operation schema
'Private Const OPER_NAMESPACE = "http://dwg.autopipes.org"
Private Const TAG_RENDER_DRAWING = "render-dwg"
Private Const TAG_GET_STATUS = "get-status"
Private Const TAG_RENDERING = "rendering"
'Private Const ATTR_AREA_ID = "area-id"
Private Const TAG_ERR_LIST = "err-entity-list"
Private Const TAG_ERR_ENTITY = "err-entity"
Private Const TAG_AREA_LIST = "area-list"
Private Const TAG_RAISER_LIST = "raiser-list"
Private Const TAG_EDGE_ID_LIST = "edge-id-list"
Private Const TAG_ID = "id"
Private Const TAG_RAISER = "raiser"
Private Const TAG_AREA = "area"
Private Const TAG_AREA_STATUS = "compliant"
Private Const TAG_AREA_MSG = "status-text"
Private Const TAG_AREA_CENTER = "area-center"
Private Const TAG_BRANCH_LABELS = "branch-labels"
Private Const TAG_MAIN_CUTS = "main-breakups"
Private Const TAG_MAIN_LABELS = "main-labels"
Private Const TAG_BRANCH_CUTS = "branch-breakups"
Private Const TAG_BRANCH_DIAMS = "branch-diameters"
Private Const TAG_MAIN_DIAMS = "main-diameters"
Private Const TAG_BRANCH_SPANS = "branch-spans"
Private Const TAG_MAIN_SPANS = "main-spans"
Private Const TAG_BRANCH_HEADS = "branch-heads"
'drawing schema
Private Const TAG_ROOT = "dwg-root"
Private Const TAG_AREA_ROOT = "area-root"
Private Const TAG_HEAD = "dwg-head"
Private Const TAG_NAME = "dwg-name"
Private Const TAG_DWG_ID = "dwg-id"
Private Const TAG_AREA_NAME = "area-name"
Private Const TAG_AREA_READINESS = "area-readiness"
Private Const TAG_AREA_ID = "area-id"
Private Const TAG_TEXT_SIZE = "dwg-text-size"
Private Const TAG_UPD_DATE = "dwg-update-date"
Private Const ENTITY_ROOT = "area-body"
Private Const ENTITY_OPT_ROOT = "area-options"
Private Const TAG_ENTITY = "dwg-entity"
Private Const TAG_START = "ent-start"
Private Const TAG_END = "ent-end"
Private Const TAG_ANG = "ent-ang"
Private Const TAG_CAPTION = "ent-caption"
Private Const TAG_SIZE = "ent-size"
Private Const AREA_ID_TOKEN = "area"
Private Const PROP_AREA_ID = TAG_AREA_ID
Private Const PROP_AREA_NAME = TAG_AREA_NAME
Private Const PROP_MAIN_START = "main-start-no"
Private Const PROP_BRANCH_START = "branch-start-no"
Private Const PROP_MAIN_LABEL = "main-label"
Private Const PROP_BRANCH_LABEL = "branch-label"

Private Const ATTR_CLS = "cls"
Private Const ATTR_COLOR = "color"
Private Const ATTR_NAME = "name"
Private Const ATTR_ID = "id"
Private Const ATTR_LAYER = "ly"
Private Const ATTR_X = "x"
Private Const ATTR_Y = "y"
Private Const ATTR_H = "h"
Private Const ATTR_W = "w"
Private Const ATTR_WRAP_LEN = "wrap-len"
Private Const ATTR_STYLE = "st"
Private Const ATTR_A = "alpha"
Private Const ATTR_B = "beta"
' config schema
Private Const CONFIG_ROOT = "config-root"
'Private Const CONFIG_NAMESPACE = "http://dwg.autopipes.org"
Private Const OPTIONS_ROOT = "options-root"
Private Const LAYERS_ROOT = "layers-root"
Private Const TAG_LAYER = "layer"
'Private Const TAG_LIN_DELTA = "linear-delta"
'Private Const TAG_ANG_DELTA = "angular-delta"
Private Const TAG_SHORT_PIPE = "short-pipe"
Private Const TAG_LONG_PIPE = "long-pipe"
Private Const TAG_BRANCH_CUT = "branch-cut"
Private Const TAG_MAIN_CUT_LIST = "main-cut-list"
Private Const TAG_BRANCH_CUT_LIST = "branch-cut-list"
Private Const TAG_MAIN_CUT = "main-cut"
Private Const TAG_MAIN_CUT_SPACE = "main-cut-space"
Private Const TAG_ERR_COLOR = "error-color"
Private Const TAG_WELD_IF_EQ = "weld-if-equal"
Private Const TAG_MAIN_PREFIX = "main-prefix"
Private Const TAG_MAIN_START = "main-count-start"
Private Const TAG_BR_PREFIX = "branch-prefix"
Private Const TAG_BR_START = "branch-count-start"

Private Const TAG_DIAM_ROOT = "headcount"
Private Const TAG_BLOCK_ATTRIB = "attributes"
Private Const TAG_ENTRY = "entry"
Private Const TAG_KEY = "key"
Private Const TAG_VALUE = "value"

Private Const ATTR_TYPE = "type"
Private Const ATTR_SUB_TYPE = "sub-type"
Private Const ATTR_VENDOR = "vendor"
Private Const ATTR_MAIN_DIAM = "main-diameter"
Private Const TAG_MAIN_DIAM = "main-diameter"
Private Const TAG_HOLE_DIAM = "hole-diameter"
Private Const ATTR_HOLE_DIAM = "hole-diameter"
Private Const TAG_BRANCH_DIAM = "branch-diameter"
Private Const ATTR_HEADS = "heads"

Private Const SUB_TYPE_GROOVED = "grooved"
Private Const SUB_TYPE_WELDED = "welded"
Private Const SUB_TYPE_WELDED_GROOVE = "weldedGroove"
Private Const SUB_TYPE_THREADED = "threaded"
Private Const VEND_VIC = "VIC"
Private Const VEND_FIRELOCK = "FIRELOCK"

Private Const LAYER_NONE = "None"
Private Const LAYER_MAIN = "Main"
Private Const LAYER_BRANCH = "Branch"
Private Const LAYER_OUT = "Output"

Private Const CLS_LINE = "AcDbLine"
Private Const CLS_MTXT = "AcDbMText"
Private Const CLS_TXT = "AcDbText"
Private Const CLS_PT = "AcDbPoint"
Private Const CLS_ARC = "AcDbArc"
Private Const CLS_CIR = "AcDbCircle"
Private Const CLS_BLK = "AcDbBlockTableRecord"
Private Const CLS_REFBLK = "AcDbBlockReference"

Private Const ONAME_EXTRACT = "EXTRACT"
Private Const ONAME_CONFIG = "CONFIG"
Private Const ONAME_LAYERS = "LAYERS"
Private Const ONAME_AREA_CONFIG = "AREA-CONFIG"
Private Const ONAME_AREA_NAME = "AREA-NAME"
Private Const ONAME_AREA_ID = "AREA-ID"
Private Const ONAME_AREA_BRANCH_LABEL = "BRANCH-LABEL"
Private Const ONAME_AREA_BRANCH_START = "BRANCH-START-NO"
Private Const ONAME_AREA_MAIN_LABEL = "MAIN-LABEL"
Private Const ONAME_AREA_MAIN_START = "MAIN-START-NO"
Private Const ONAME_AREA_IGNORE_LIMIT = "IGNORE-SHORTER-THAN"
Private Const ONAME_AREA_CUT_LIMIT = "CUT-LONGER-THAN"
Private Const ONAME_AREA_CUT_SIZE_B = "CUT-BRANCH-INTO"
Private Const ONAME_AREA_CUT_SIZE_M = "CUT-MAIN-INTO"
Private Const ONAME_AREA_CUT_CLEARANCE = "MAIN-CUT-CLEARANCE"

Private Const FT2IN = 12#

Private goAutoCAD As AcadApplication
Private goModel As AcadModelSpace
Private goDoc As MSXML2.DOMDocument
Private goConf As MSXML2.DOMDocument
Private goStatus As MSXML2.DOMDocument
Private gsDwgName As String
Private gsDwgDir As String
Private daMainDiam(0 To 7) As Integer
Private nFirstMainDiamIdx As Integer
Private gbSaveWithDefaults As Boolean
Private gdWidthFactor As Double
Private gdTextSize As Double
Private gsUpdateDate As String

Private Function date2string(w As Date) As String
   Dim s As String
   Dim tms As String
   Dim tm
   Dim idx As Integer
   
   s = w
   idx = InStr(s, " ")
   tms = Mid(s, idx + 1)
   tm = TimeValue(tms)
   
  'the line below fails on acad2k and gives only minutes resolution
  'date2string = Year(w) & "-" & Format(Month(w), "00") & "-" & Format(Day(w), "00") & "T" & FormatDateTime(w, vbShortTime) & ":00"
  date2string = Year(w) & "-" & Format(Month(w), "00") & "-" & Format(Day(w), "00") & "T" & Format(Hour(tm), "00") & ":" & Format(Minute(tm), "00") & ":" & Format(Second(tm), "00")
End Function
Public Function validateAcad(prompt As Boolean) As Boolean
  validateAcad = True
  If gdTextSize = 0 Then
    If prompt Then
        MsgBox ("Please define text size")
    End If
    validateAcad = False
  End If
  If gsUpdateDate = "" Then
    If prompt Then
        MsgBox ("Please save the drawing")
    End If
    validateAcad = False
  End If
End Function
Private Sub Class_Initialize()
  Set goAutoCAD = ThisDrawing.Application
  Set goModel = goAutoCAD.ActiveDocument.ModelSpace()
  Set goDoc = New MSXML2.DOMDocument
  Set goConf = New MSXML2.DOMDocument
  Set goStatus = New MSXML2.DOMDocument
  gsDwgDir = ThisDrawing.Path
  gsDwgName = ThisDrawing.name
  If Dir(gsDwgDir & "\" & gsDwgName) <> "" Then
     gsUpdateDate = date2string(FileDateTime(gsDwgDir & "\" & gsDwgName))
  End If
  gsDwgName = Left(gsDwgName, Len(gsDwgName) - 4)
  ' In ACAD 2K, width factor has to be set explicitly on each tex object
  gdWidthFactor = ThisDrawing.ActiveTextStyle.Width
  gdTextSize = ThisDrawing.Application.ActiveDocument.ActiveTextStyle.Height
  
  If validateAcad(False) = False Then
    Exit Sub
  End If
  
  Call initAreaConfig
  Call InitConfig
  
  daMainDiam(0) = 100
  daMainDiam(1) = 125
  daMainDiam(2) = 150
  daMainDiam(3) = 200
  daMainDiam(4) = 250
  daMainDiam(5) = 300
  daMainDiam(6) = 400
  daMainDiam(7) = 600

  nFirstMainDiamIdx = 2
  gbSaveWithDefaults = False
End Sub
Public Sub setSaveWithDefaults(bVal As Boolean)
  gbSaveWithDefaults = bVal
End Sub

Public Function FindDiameterIndex(nDiam As Integer) As Integer
Dim I As Integer
 I = 0
 While (nDiam <> daMainDiam(I))
   I = I + 1
 Wend
 FindDiameterIndex = I
End Function
Public Function firstMainDiameterDigits(sType As String) As Integer
  Dim nIdx As Integer
  nIdx = 0
  If sType = SUB_TYPE_GROOVED Then
    nIdx = nFirstMainDiamIdx
  End If
  firstMainDiameterDigits = getDiameterDigits(nIdx)
End Function
Public Function getDiameterDigits(nIndex As Integer) As Integer
   If nIndex < 0 Then
     nIndex = UBound(daMainDiam)
   End If
   getDiameterDigits = daMainDiam(nIndex)
End Function
Private Sub Class_Terminate()
 Call SaveConfig
End Sub
Private Sub updateHeaderProp(tag As String, val As Variant)
'Dim oHead As IXMLDOMElement
Dim oElement As IXMLDOMElement
Dim oList As IXMLDOMNodeList
Dim oText As IXMLDOMText
    Dim sXpath As String
    Call goConf.setProperty("SelectionLanguage", "XPath")

    sXpath = "*[local-name()='" & tag & "']"
    'Set oHead = goConf.documentElement.firstChild
    Set oList = goConf.documentElement.selectNodes(sXpath)
    If oList.Length = 0 Then
       Set oElement = createCfgElement(tag, val)
       Call goConf.documentElement.appendChild(oElement)
    Else
       Set oElement = oList.Item(0)
       If oElement.childNodes.Length > 0 Then
           Set oText = oElement.firstChild
           oText.text = val
       Else
           Set oText = goConf.createTextNode(val)
           Call oElement.appendChild(oText)
       End If
    End If
End Sub
Private Sub initAreaConfig()
Dim aPoint(0 To 2) As Double
Dim aAttrib(1 To 11) As String
Dim oBlock As AcadBlock
Dim oAttr As AcadAttribute
Dim eMode As AcAttributeMode
Dim I As Integer
aPoint(0) = 0
aPoint(1) = 0
aPoint(2) = 0
aAttrib(1) = ONAME_AREA_ID
aAttrib(2) = ONAME_AREA_NAME
aAttrib(3) = ONAME_AREA_MAIN_LABEL
aAttrib(4) = ONAME_AREA_MAIN_START
aAttrib(5) = ONAME_AREA_BRANCH_LABEL
aAttrib(6) = ONAME_AREA_BRANCH_START
aAttrib(7) = ONAME_AREA_IGNORE_LIMIT
aAttrib(8) = ONAME_AREA_CUT_LIMIT
aAttrib(9) = ONAME_AREA_CUT_SIZE_B
aAttrib(10) = ONAME_AREA_CUT_SIZE_M
aAttrib(11) = ONAME_AREA_CUT_CLEARANCE

  Set oBlock = GetBlock(ONAME_AREA_CONFIG)
  For I = 1 To 11
    eMode = acAttributeModeInvisible
    If aAttrib(I) = ONAME_AREA_NAME Then
      eMode = acAttributeModeNormal
    End If
    Set oAttr = GetBlockAttribute(oBlock, aAttrib(I), eMode)
    oAttr.InsertionPoint = aPoint
    aPoint(1) = aPoint(1) - gdTextSize * 1.2
  Next
End Sub
Private Sub InitConfig()
' Called by initialization after acad props have been copied to globals
' Responsible for setting goConf global.
' For clean drawing initializes it with defaults
' For saved drawing, loads it with the content of oname_layers block but updates name, ts, font

Dim oBlock As AcadBlock
Dim oAttr As AcadAttribute
Dim oElement As IXMLDOMElement
Dim oOpt As IXMLDOMElement
Dim oText As IXMLDOMText

  Set oBlock = GetBlock(ONAME_CONFIG)
  Set oAttr = GetBlockAttribute(oBlock, ONAME_LAYERS, acAttributeModeConstant)
  If oAttr.TextString <> "" Then
  ' retrieve saved configuration
    Call goConf.loadXML(oAttr.TextString)
    ' update dwg name in case of rename
    Call updateHeaderProp(TAG_NAME, gsDwgName)
    Call updateHeaderProp(TAG_TEXT_SIZE, gdTextSize)
    Call updateHeaderProp(TAG_UPD_DATE, gsUpdateDate)
  Else
  ' new configuration
  'root & head
    Call initCfgDoc
  'options
    Set oOpt = createCfgElement(OPTIONS_ROOT)
    Call goConf.documentElement.appendChild(oOpt)
  'layers
    Set oElement = createCfgElement(LAYERS_ROOT)
    Call oOpt.appendChild(oElement)
  End If
End Sub
Public Function FindLayerNode(sLayer As String) As IXMLDOMNodeList
Dim sXpath As String
  Call goConf.setProperty("SelectionLanguage", "XPath")

'  sXpath = "./" & OPTIONS_ROOT & "/" & LAYERS_ROOT & "/" & TAG_LAYER & "[@" & ATTR_NAME & "=""" & sLayer & """]"
  sXpath = "*[local-name()='" & OPTIONS_ROOT & "']/*[local-name()='" & LAYERS_ROOT & "']/*[local-name()='" & TAG_LAYER & "'][@" & ATTR_NAME & "='" & sLayer & "']"
  Set FindLayerNode = goConf.documentElement.selectNodes(sXpath)
End Function

Public Function FindLayerNodesByType(sType As String) As IXMLDOMNodeList
Dim sXpath As String
  sXpath = "*[local-name()='" & OPTIONS_ROOT & "']/*[local-name()='" & LAYERS_ROOT & "']/*[local-name()='" & TAG_LAYER & "'][@" & ATTR_TYPE & "='" & sType & "']"
  Set FindLayerNodesByType = goConf.documentElement.selectNodes(sXpath)
End Function
Public Function GetLayerType(sLayer As String) As String
Dim oLayerNodes As IXMLDOMNodeList
Dim oLayer As IXMLDOMElement
  Set oLayerNodes = FindLayerNode(sLayer)
  If oLayerNodes.Length = 0 Then
     GetLayerType = NextLayerType("")
  Else
     Set oLayer = oLayerNodes.Item(0)
     GetLayerType = oLayer.getAttribute(ATTR_TYPE)
  End If
End Function
Private Function createDocElement(name As String, Optional value As Variant = "") As IXMLDOMNode
  Set createDocElement = createElement(goDoc, name, value)
End Function
Private Function createCfgElement(name As String, Optional value As Variant = "") As IXMLDOMNode
  Set createCfgElement = createElement(goConf, name, value)
End Function
Private Function createElement(doc As MSXML2.DOMDocument, name As String, Optional value As Variant = "") As IXMLDOMNode
Dim oText As IXMLDOMText
  Set createElement = doc.createNode("ELEMENT", name, DWG_NAMESPACE)
  If value <> "" Then
        Set oText = doc.createTextNode(value)
        Call createElement.appendChild(oText)
  End If
End Function
Public Function getVendor(sLayer As String) As String
  getVendor = getMainLayerAttribute(sLayer, ATTR_VENDOR)
End Function
Public Function getMainDiameter(sLayer As String) As Integer
Dim sEnum As String
  sEnum = getMainLayerAttribute(sLayer, ATTR_MAIN_DIAM)
  getMainDiameter = EnumToDiameterDigits(sEnum)
End Function
Public Function getHoleDiameter(sLayer As String) As Integer
Dim sEnum As String
  sEnum = getMainLayerAttribute(sLayer, ATTR_HOLE_DIAM)
  getHoleDiameter = EnumToDiameterDigits(sEnum)
End Function
Public Function getMainType(sLayer As String) As String
  getMainType = getMainLayerAttribute(sLayer, ATTR_SUB_TYPE)
End Function
'Public Function getLinearDelta() As Double
'  getLinearDelta = getOption(TAG_LIN_DELTA, -1)
'End Function
'Public Function getAngularDelta() As Double
'  getAngularDelta = getOption(TAG_ANG_DELTA, -1)
'End Function
Public Function getErrorColor() As String
  getErrorColor = getOption(TAG_ERR_COLOR, "")
End Function
Public Function getMainPrefix() As String
  getMainPrefix = getOption(TAG_MAIN_PREFIX, "")
End Function
Public Function getMainStart() As Long
  getMainStart = getOption(TAG_MAIN_START, -1)
End Function
Public Function getBrPrefix() As String
  getBrPrefix = getOption(TAG_BR_PREFIX, "")
End Function
Public Function getBrStart() As Long
  getBrStart = getOption(TAG_BR_START, -1)
End Function

Public Function getShortPipe() As Double
  getShortPipe = getOption(TAG_SHORT_PIPE, -1)
End Function
Public Function getLongPipe() As Double
  Dim dVal As Double
  dVal = getOption(TAG_LONG_PIPE, -1)
  getLongPipe = dVal / FT2IN
End Function
Public Function getBranchCut() As String
  getBranchCut = getCut(TAG_BRANCH_CUT_LIST, TAG_BRANCH_CUT)

  'Dim dVal As Double
  'dVal = getOption(TAG_BRANCH_CUT, -1)
  'getBranchCut = dVal / FT2IN
End Function
Public Function getMainCutSpace() As Double
  getMainCutSpace = getOption(TAG_MAIN_CUT_SPACE, -1)
End Function
Public Function getMainCut() As String
    getMainCut = getCut(TAG_MAIN_CUT_LIST, TAG_MAIN_CUT)
End Function

Private Function getCut(sListTag As String, sCutTag As String) As String
Dim oNodes As IXMLDOMNodeList
Dim oOptionsRoot As IXMLDOMElement
Dim oListNode As IXMLDOMElement
Dim dVal As Double
Dim I As Integer
     getCut = ""
     Set oOptionsRoot = goConf.documentElement.getElementsByTagName(OPTIONS_ROOT).Item(0)
     Set oNodes = oOptionsRoot.getElementsByTagName(sListTag)
     If oNodes.Length > 0 Then
       Set oListNode = oNodes.Item(0)
       Set oNodes = oListNode.getElementsByTagName(sCutTag)
       For I = 0 To oNodes.Length - 1
         dVal = oNodes.Item(I).childNodes.Item(0).text
         getCut = getCut & " " & (dVal / FT2IN)
       Next I
     End If
End Function
Public Function getOption(sTag As String, vDflt As Variant) As Variant
Dim oVendorNodes As IXMLDOMNodeList
Dim oOptionsRoot As IXMLDOMElement
     Set oOptionsRoot = goConf.documentElement.getElementsByTagName(OPTIONS_ROOT).Item(0)
     Set oVendorNodes = oOptionsRoot.getElementsByTagName(sTag)
     If oVendorNodes.Length = 0 Then
       getOption = vDflt
     Else
       getOption = oVendorNodes.Item(0).childNodes.Item(0).text
     End If
End Function
Public Sub setVendor(sLayer As String, sVal As String)
  Call setMainLayerAttribute(sLayer, ATTR_VENDOR, sVal)
End Sub
Public Sub setMainDiameter(sLayer As String, nDigits As Integer)
  Dim sEnum As String
  sEnum = DiameterDigitsToEnum(nDigits)
  Call setMainLayerAttribute(sLayer, ATTR_MAIN_DIAM, sEnum)
End Sub
Public Sub setHoleDiameter(sLayer As String, nDigits As Integer)
  Dim sEnum As String
  sEnum = DiameterDigitsToEnum(nDigits)
  Call setMainLayerAttribute(sLayer, ATTR_HOLE_DIAM, sEnum)
End Sub
Public Sub setMainType(sLayer As String, sVal As String)
  Call setMainLayerAttribute(sLayer, ATTR_SUB_TYPE, sVal)
End Sub
'Public Sub setLinearDelta(dVal As Double)
'  Call setOption(TAG_LIN_DELTA, dVal)
'end Sub
Public Sub setErrorColor(sVal As String)
  Call setOption(TAG_ERR_COLOR, sVal)
End Sub
Public Sub setMainPrefix(sVal As String)
  Call setOption(TAG_MAIN_PREFIX, sVal)
End Sub
Public Sub setMainStart(nVal As Long)
  Call setOption(TAG_MAIN_START, nVal)
End Sub
Public Sub setBrPrefix(sVal As String)
  Call setOption(TAG_BR_PREFIX, sVal)
End Sub
Public Sub setBrStart(nVal As Long)
  Call setOption(TAG_BR_START, nVal)
End Sub
'Public Sub setAngularDelta(dVal As Double)
'  Call setOption(TAG_ANG_DELTA, dVal)
'End Sub
Public Sub setShortPipe(dVal As Double)
  Call setOption(TAG_SHORT_PIPE, dVal)
End Sub
Public Sub setLongPipe(dVal As Double)
  Call setOption(TAG_LONG_PIPE, FT2IN * dVal)
End Sub
Public Sub setBranchCut(sVal As String)
  'Call setOption(TAG_BRANCH_CUT, FT2IN * dVal)
  Call setCut(sVal, TAG_BRANCH_CUT_LIST, TAG_BRANCH_CUT)
End Sub
Public Sub setMainCutSpace(dVal As Double)
  Call setOption(TAG_MAIN_CUT_SPACE, dVal)
End Sub
Public Sub setMainCut(sVal As String)
  Call setCut(sVal, TAG_MAIN_CUT_LIST, TAG_MAIN_CUT)
End Sub
Private Sub setCut(sVal As String, sListTag As String, sCutTag As String)
Dim oOptionsRoot As IXMLDOMElement
Dim oNodes As IXMLDOMNodeList
Dim oListNode As IXMLDOMElement
Dim oNode As IXMLDOMElement
'Dim A() As String
'Dim i As Integer
Dim dVal As Double
Dim sTok As String
Dim sRest As String
Dim vRet As Variant
     Set oOptionsRoot = goConf.documentElement.getElementsByTagName(OPTIONS_ROOT).Item(0)
     Set oNodes = oOptionsRoot.getElementsByTagName(sListTag)
     If oNodes.Length > 0 Then
       Call oOptionsRoot.removeChild(oNodes.Item(0))
     End If
     Set oListNode = createCfgElement(sListTag)
     Call oOptionsRoot.appendChild(oListNode)
    ' Not available in ACAD 2K
    ' A = Split(sVal)
    ' For i = 0 To UBound(A)
    '   Set oNode = createCfgElement(TAG_MAIN_CUT, A(i))
    '   Call oListNode.appendChild(oNode)
    'Next i
    sRest = LTrim(sVal)
    Do
      If Len(sRest) = 0 Then
        Exit Do
      End If
      vRet = firstToken(sRest)
      sTok = vRet(0)
      sRest = vRet(1)
      dVal = sTok
      Set oNode = createCfgElement(sCutTag, FT2IN * dVal)
      Call oListNode.appendChild(oNode)
    Loop
End Sub

Public Function IsSequenceNumeric(seq As String) As Boolean
'Dim A() As String
'Dim i As Integer
Dim sTok As String
Dim sRest As String
Dim vRet As Variant
  IsSequenceNumeric = True
  sRest = LTrim(seq)
  Do
    If Len(sRest) = 0 Then
      Exit Do
    End If
    vRet = firstToken(sRest)
    sTok = vRet(0)
    sRest = vRet(1)
    If Not (IsNumeric(sTok)) Then
      IsSequenceNumeric = False
      Exit Do
    End If
  Loop
End Function
' inString is Ltrim'ed.
' Returns array where item 0 is the first token and item 1
' is ltrimed substring starting at the next token
' or empty string if no more tokens.
Public Function firstToken(inString) As Variant
Dim idx As Integer
Dim token As String
Dim rest As String
   idx = InStr(1, inString, " ")
   If idx = 0 Then
     token = inString
     rest = ""
   Else
     token = Mid(inString, 1, idx - 1)
     rest = LTrim(Mid(inString, idx + 1))
   End If
   firstToken = Array(token, rest)
End Function
Public Function firstSeparatedToken(inString As String, sep As String) As Variant
Dim idx As Integer
Dim token As String
Dim rest As String
   idx = InStr(1, inString, sep)
   If idx = 0 Then
     token = inString
     rest = ""
   Else
     token = Mid(inString, 1, idx - 1)
     rest = LTrim(Mid(inString, idx + Len(sep)))
   End If
   firstSeparatedToken = Array(token, rest)
End Function


Public Sub setOption(sTag As String, sVal As Variant)
Dim oVendorNodes As IXMLDOMNodeList
Dim oOptionsRoot As IXMLDOMElement
Dim oText As IXMLDOMText
Dim oVendor As IXMLDOMElement
     Set oOptionsRoot = goConf.documentElement.getElementsByTagName(OPTIONS_ROOT).Item(0)
     Set oVendorNodes = oOptionsRoot.getElementsByTagName(sTag)
     If oVendorNodes.Length = 0 Then
       Set oVendor = createCfgElement(sTag, sVal)
       Call oOptionsRoot.appendChild(oVendor)
     Else
       Set oVendor = oVendorNodes.Item(0)
       Set oText = oVendor.childNodes.Item(0)
       oText.text = sVal
     End If
End Sub
Public Sub SetLayerAttributes(sLayer As String, sType As String, sSubType As String, sVendor As String)
Dim oLayerNodes As IXMLDOMNodeList
Dim oLayer As IXMLDOMElement
Dim oOpt As IXMLDOMElement
Dim oLayerRoot As IXMLDOMElement
  Set oLayerNodes = FindLayerNode(sLayer)
  If oLayerNodes.Length = 0 Then
     Set oLayer = createCfgElement(TAG_LAYER)
     Call oLayer.setAttribute(ATTR_NAME, sLayer)
     Set oOpt = goConf.documentElement.getElementsByTagName(OPTIONS_ROOT).Item(0)
     Set oLayerRoot = oOpt.getElementsByTagName(LAYERS_ROOT).Item(0)
     Call oLayerRoot.appendChild(oLayer)
  Else
     Set oLayer = oLayerNodes.Item(0)
  End If
  Call oLayer.setAttribute(ATTR_TYPE, sType)
  If sType = LAYER_MAIN Then
    Call oLayer.setAttribute(ATTR_SUB_TYPE, sSubType)
    Call oLayer.setAttribute(ATTR_VENDOR, sVendor)
  Else
    Call oLayer.removeAttribute(ATTR_SUB_TYPE)
    Call oLayer.removeAttribute(ATTR_VENDOR)
  End If
End Sub
Private Sub setMainLayerAttribute(sLayer As String, sAttrib As String, sVal As String)
Dim mList As IXMLDOMNodeList
Dim I As Integer
Dim oLayer As IXMLDOMElement
Dim sType As String
  Set mList = FindLayerNode(sLayer) '  FindLayerNodesByType(LAYER_MAIN)
  If mList.Length > 0 Then
    Set oLayer = mList.Item(0)
    sType = oLayer.getAttribute(ATTR_TYPE)
    If sType = LAYER_MAIN Then
      If sVal <> "" Then
        Call oLayer.setAttribute(sAttrib, sVal)
      Else
        Call oLayer.removeAttribute(sAttrib)
      End If
    End If
  End If
End Sub
Private Function getMainLayerAttribute(sLayer As String, sAttrib As String) As String
Dim mList As IXMLDOMNodeList
Dim oLayer As IXMLDOMElement
Dim sType As String
  Set mList = FindLayerNode(sLayer) '     FindLayerNodesByType(LAYER_MAIN)
  If mList.Length > 0 Then
    Set oLayer = mList.Item(0)
    sType = oLayer.getAttribute(ATTR_TYPE)
    If sType <> LAYER_MAIN Then
      getMainLayerAttribute = ""
    ElseIf IsNull(oLayer.getAttribute(sAttrib)) Then
      getMainLayerAttribute = ""
    Else
      getMainLayerAttribute = oLayer.getAttribute(sAttrib)
    End If
  Else
    getMainLayerAttribute = ""
  End If
End Function

Public Function DiameterDisplayToDigits(sDiamDist As String) As Integer
Dim nLen As Integer
Dim cLast As String
Dim nRet As Integer
If sDiamDist = "" Then
   nRet = 0
Else
   nLen = Len(sDiamDist)
   cLast = Right(sDiamDist, 1)
   If cLast = Chr(188) Then
     nRet = 25
   ElseIf cLast = Chr(189) Then
     nRet = 50
   Else
     nRet = 0
   End If
   If (nRet = 0) Then
          nRet = 100 * sDiamDist
   Else
          nRet = 100 * Left(sDiamDist, nLen - 1) + nRet
   End If
End If
'   If nLen = 1 Then
'     nRet = 100 * sDiamDist
'   Else
'     nRet = Left(sDiamDist, 1) * 100
'     If (cLast = Chr(189)) Then ' compare to 1/2
'       nRet = nRet + 50
'     Else
'       nRet = nRet + 25
'     End If
'   End If
   DiameterDisplayToDigits = nRet
End Function
Public Function DiameterDigitsToDisplay(nDiamDigits As Integer) As String
 Dim nFract As Integer
 Dim sFract As String
 nFract = nDiamDigits - (nDiamDigits \ 100) * 100
 If nFract = 25 Then
  sFract = Chr(188) ' " 1/4"
 ElseIf nFract = 50 Then
  sFract = Chr(189) '" 1/2"
 Else
  sFract = ""
 End If
 DiameterDigitsToDisplay = ((nDiamDigits - nFract) / 100) & sFract
End Function
Public Function EnumToDiameterDigits(sEnum As String) As Integer
Dim sRet As String
If sEnum = "" Then
  EnumToDiameterDigits = 0
ElseIf sEnum = "D05" Then
  EnumToDiameterDigits = 50
Else
  sRet = sEnum
  If Len(sEnum) = 3 Then
    sRet = sEnum & "0"
  ElseIf Len(sEnum) = 2 Then
    sRet = sEnum & "00"
  End If
  EnumToDiameterDigits = Right(sRet, 3)
End If
End Function
Public Function DiameterDigitsToEnum(nDiamDigits As Integer) As String
Dim nRet As Integer
Dim I As Integer
' strip trailing 0s
nRet = nDiamDigits
If nRet <> 0 Then
 For I = 1 To 2
  If (nRet \ 10) * 10 = nRet Then
    nRet = nRet \ 10
  Else
    Exit For
  End If
 Next
End If
If nRet = 0 Then
  DiameterDigitsToEnum = ""
ElseIf nRet = 5 Then
  DiameterDigitsToEnum = "D05"
Else
  DiameterDigitsToEnum = "D" & nRet
End If
End Function
Private Function findDiameterNode(oLayer As IXMLDOMNode, nHeadCount As Integer) As IXMLDOMNodeList
Dim sTag As String
Dim sXpath As String
  If nHeadCount = 0 Then
    sTag = TAG_MAIN_DIAM
  ElseIf nHeadCount = -1 Then
    sTag = TAG_HOLE_DIAM
  Else
    sTag = TAG_BRANCH_DIAM
  End If
  sXpath = "./" & sTag
  If nHeadCount > 0 Then
    sXpath = sXpath & "[@" & ATTR_HEADS & "=""" & nHeadCount & """]"
  End If
  Set findDiameterNode = oLayer.selectNodes(sXpath)
End Function
Private Function findDiameterEntry(oLayer As IXMLDOMNode, nHeadCount As Integer) As IXMLDOMNodeList
Dim sXpath As String
'  sXpath = "./" & TAG_DIAM_ROOT & "/" & TAG_ENTRY & "[" & TAG_KEY & "=""" & nHeadCount & """]"
  sXpath = "*[local-name()='" & TAG_DIAM_ROOT & "']/" & TAG_ENTRY & "[" & TAG_KEY & "=""" & nHeadCount & """]"
  
  Set findDiameterEntry = oLayer.selectNodes(sXpath)
End Function
Private Function findMapEntries(oParent As IXMLDOMNode, sMapTag As String) As IXMLDOMNodeList
Dim sXpath As String
  sXpath = "*[local-name()='" & sMapTag & "']/" & TAG_ENTRY
  
  Set findMapEntries = oParent.selectNodes(sXpath)
End Function
Private Function findMapEntry(oParent As IXMLDOMNode, sMapTag As String, vKey As Variant) As IXMLDOMNodeList
Dim sXpath As String
  sXpath = "*[local-name()='" & sMapTag & "']/" & TAG_ENTRY & "[" & TAG_KEY & "=""" & vKey & """]"
  
  Set findMapEntry = oParent.selectNodes(sXpath)
End Function
Private Function findEntryValue(oEntry As IXMLDOMElement) As String
  Dim oValue As IXMLDOMElement
  Set oValue = oEntry.getElementsByTagName(TAG_VALUE).Item(0)
  findEntryValue = oValue.text
End Function
Private Function findEntryKey(oEntry As IXMLDOMElement) As String
  Dim oValue As IXMLDOMElement
  Set oValue = oEntry.getElementsByTagName(TAG_KEY).Item(0)
  findEntryKey = oValue.text
End Function

Private Function findMapEntryValue(oParent As IXMLDOMNode, sMapTag As String, vKey As Variant, vDflt As Variant) As Variant
Dim oEntry As IXMLDOMNodeList
  Set oEntry = findMapEntry(oParent, sMapTag, vKey)
  If oEntry.Length = 0 Then
    findMapEntryValue = vDflt
  Else
    findMapEntryValue = findEntryValue(oEntry.Item(0))
  End If
End Function

Public Function GetLayerDiameter(sLayer As String, nHeadCount As Integer) As Integer
Dim oText As IXMLDOMText
Dim oDiamNodes As IXMLDOMNodeList
Dim oDiamEntry As IXMLDOMElement
Dim oDiamValue As IXMLDOMElement
Dim oLayer As IXMLDOMElement
Dim oLayerNodes As IXMLDOMNodeList
Dim sType As String
Dim nBranchDiamNum As Integer
Dim sDiamEnum As String
  Set oLayerNodes = FindLayerNode(sLayer)
  If oLayerNodes.Length = 0 Then
      GetLayerDiameter = 0
  Else
    Set oLayer = oLayerNodes.Item(0)
    Set oDiamNodes = findDiameterEntry(oLayer, nHeadCount)
    If oDiamNodes.Length = 0 Then
      GetLayerDiameter = 0
    Else
      Set oDiamEntry = oDiamNodes.Item(0)
      Set oDiamValue = oDiamEntry.getElementsByTagName(TAG_VALUE).Item(0)
      Set oText = oDiamValue.childNodes.Item(0)
      sDiamEnum = oText.text
      GetLayerDiameter = EnumToDiameterDigits(sDiamEnum)
    End If
  End If
End Function
Private Function createMapEntry(doc As MSXML2.DOMDocument, name As String, val As Variant) As IXMLDOMElement
Dim oKey As IXMLDOMElement
Dim oVal As IXMLDOMElement
Dim oText As IXMLDOMText
    Set createMapEntry = doc.createElement(TAG_ENTRY)
    Set oKey = doc.createElement(TAG_KEY)
    Call createMapEntry.appendChild(oKey)
    Set oVal = doc.createElement(TAG_VALUE)
    Call createMapEntry.appendChild(oVal)
    Set oText = doc.createTextNode(name)
    Call oKey.appendChild(oText)
    Set oText = doc.createTextNode(val)
    Call oVal.appendChild(oText)
End Function
Public Sub SetLayerDiameter(sLayer As String, nHeadCount As Integer, nVal As Integer)
Dim oLayer As IXMLDOMElement
Dim oDiamRoot As IXMLDOMElement
Dim oDiamKey As IXMLDOMElement
Dim oDiamValue As IXMLDOMElement
Dim oDiamEntry As IXMLDOMElement
Dim oNodes As IXMLDOMNodeList
Dim oText As IXMLDOMText
  Set oLayer = FindLayerNode(sLayer).Item(0)
  ' make sure diameter root is there
  Set oNodes = oLayer.getElementsByTagName(TAG_DIAM_ROOT)
  If oNodes.Length = 0 Then
    Set oDiamRoot = createCfgElement(TAG_DIAM_ROOT)
    Call oLayer.appendChild(oDiamRoot)
  Else
    Set oDiamRoot = oNodes.Item(0)
  End If
  Set oNodes = findDiameterEntry(oLayer, nHeadCount)
  If oNodes.Length = 0 Then
    Set oDiamEntry = goConf.createElement(TAG_ENTRY)
    Call oDiamRoot.appendChild(oDiamEntry)
    Set oDiamKey = goConf.createElement(TAG_KEY)
    Call oDiamEntry.appendChild(oDiamKey)
    Set oDiamValue = goConf.createElement(TAG_VALUE)
    Call oDiamEntry.appendChild(oDiamValue)
    Set oText = goConf.createTextNode(nHeadCount)
    Call oDiamKey.appendChild(oText)
    Set oText = goConf.createTextNode(DiameterDigitsToEnum(nVal))
    Call oDiamValue.appendChild(oText)
  Else
    Set oDiamEntry = oNodes.Item(0)
    Set oDiamValue = oDiamEntry.getElementsByTagName(TAG_VALUE).Item(0)
    Set oText = oDiamValue.childNodes.Item(0)
    oText.text = DiameterDigitsToEnum(nVal)
  End If
End Sub
Public Sub SetLayerDiameterOld(sLayer As String, nHeadCount As Integer, nVal As Integer)
Dim oDiamNodes As IXMLDOMNodeList
Dim oDiam As IXMLDOMElement
Dim oText As IXMLDOMText
Dim oLayer As IXMLDOMElement
Dim sTag As String
Dim dVal As Variant
  dVal = CDec(nVal / 100)
  If nHeadCount = 0 Then
    sTag = TAG_MAIN_DIAM
  ElseIf nHeadCount = -1 Then
    sTag = TAG_HOLE_DIAM
  Else
    sTag = TAG_BRANCH_DIAM
  End If
  Set oLayer = FindLayerNode(sLayer).Item(0)
  Set oDiamNodes = findDiameterNode(oLayer, nHeadCount)

  If oDiamNodes.Length = 0 Then
  ' this diameter node does not exist
     Set oDiam = createCfgElement(sTag, dVal)
     If nHeadCount > 0 Then
        Call oDiam.setAttribute(ATTR_HEADS, nHeadCount)
     End If
     If sTag = TAG_MAIN_DIAM And oLayer.hasChildNodes Then
     ' main always first
        Call oLayer.insertBefore(oDiam, oLayer.firstChild)
     ElseIf sTag = TAG_HOLE_DIAM And oLayer.hasChildNodes Then
     ' hole after main or first if no main
        If oLayer.firstChild.nodeName = TAG_MAIN_DIAM Then
          If oLayer.childNodes.Length > 1 Then
             Call oLayer.insertBefore(oDiam, oLayer.firstChild.nextSibling)
          Else
             Call oLayer.appendChild(oDiam)
          End If
        Else
          Call oLayer.insertBefore(oDiam, oLayer.firstChild)
        End If
     Else
        Call oLayer.appendChild(oDiam)
     End If
     'Set oText = goConf.createTextNode(dVal)
     'Call oDiam.appendChild(oText)
  Else
     Set oDiam = oDiamNodes.Item(0)
     Set oText = oDiam.childNodes.Item(0)
     oText.text = dVal
  End If
End Sub
Private Sub SaveConfig()
Dim oBlock As AcadBlock
Dim oAttr As AcadAttribute
  Set oBlock = GetBlock(ONAME_CONFIG)
  Set oAttr = GetBlockAttribute(oBlock, ONAME_LAYERS, acAttributeModeConstant)
  If gbSaveWithDefaults Then
      oAttr.TextString = ""
  Else
      oAttr.TextString = goConf.xml
  End If
End Sub
'Retrieves block attribute from a block, creating one if necessary
Private Function GetBlockAttribute(oBlock As AcadBlock, sAttrName As String, eMode As AcAttributeMode) As AcadAttribute
Dim aPoint(0 To 2) As Double
Dim bPresent As Boolean
Dim oEnt As AcadEntity
    bPresent = False
    For Each oEnt In oBlock
         If oEnt.ObjectName = "AcDbAttributeDefinition" Then
           Set GetBlockAttribute = oEnt
           If GetBlockAttribute.TagString = sAttrName Then
            bPresent = True
            Exit For
           End If
         End If
    Next oEnt
    If Not bPresent Then
       aPoint(0) = 0
       aPoint(1) = 0
       aPoint(2) = 0
      Set GetBlockAttribute = oBlock.AddAttribute(gdTextSize, eMode, sAttrName, aPoint, sAttrName, "")
    End If
End Function
'Retrieves block definition from the drawing creating an empty one if necessary
Private Function GetBlock(sName As String) As AcadBlock
Dim aPoint(0 To 2) As Double
Dim oBlock As AcadBlock
Dim bPresent As Boolean
  bPresent = False
  For Each oBlock In goAutoCAD.ActiveDocument.Blocks
    If oBlock.name = sName Then
      Set GetBlock = oBlock
      bPresent = True
      Exit For
    End If
  Next oBlock
  If Not bPresent Then
    aPoint(0) = 0
    aPoint(1) = 0
    aPoint(2) = 0
    Set GetBlock = goAutoCAD.ActiveDocument.Blocks.Add(aPoint, sName)
  End If
End Function
'Private Sub CreateFitting()
'Dim oBlock As AcadBlock
'Dim aPoint(0 To 2) As Double
'Dim bPresent As Boolean
'Dim oRef As AcadBlockReference
'Dim varAttributes As Variant
'
'  For Each oBlock In goAutoCAD.ActiveDocument.Blocks
'    If oBlock.name = ONAME_FITTING Then
'      bPresent = True
'      Exit For
'    End If
'  Next oBlock
'  If Not bPresent Then
'    aPoint(0) = 0
'    aPoint(1) = 0
'    aPoint(2) = 0
'    Set oBlock = goAutoCAD.ActiveDocument.Blocks.Add(aPoint, ONAME_FITTING)
'  End If
'  Call oBlock.AddAttribute(1#, acAttributeModeConstant, "", aPoint, "OBJ", "0")
'  Set oRef = goModel.InsertBlock(aPoint, ONAME_FITTING, 1, 1, 1, 0)
'  varAttributes = oRef.GetConstantAttributes
'  varAttributes(0).TagString = "OBJ1"
'  varAttributes(0).TextString = "100"
'
'End Sub
Public Sub UpdateText(oEntity As AcadEntity, oElement As IXMLDOMElement, strClass As String)
Dim oSubNodes As IXMLDOMNodeList
Dim oSubElement As IXMLDOMElement
Dim pos(0 To 2) As Double
Dim dSize As Double
Dim dWrapLen As Double
Dim sText As String
Dim dAng As Double
Dim oText As AcadText
Dim oMText As AcadMText
'find start
  Set oSubElement = getElementByTagName(oElement, TAG_START)
  pos(0) = oSubElement.getAttribute(ATTR_X)
  pos(1) = oSubElement.getAttribute(ATTR_Y)
  pos(2) = 0#
'find text sizes
  Set oSubElement = getElementByTagName(oElement, TAG_SIZE)
  dSize = oSubElement.getAttribute(ATTR_H)
  If strClass = CLS_MTXT Then
    dWrapLen = oSubElement.getAttribute(ATTR_W)
    Set oMText = oEntity
    oMText.Width = dWrapLen
  End If
'find text
  Set oSubElement = getElementByTagName(oElement, TAG_CAPTION)
  sText = oSubElement.text
'find angle
  Set oSubNodes = getElementsByTagName(oElement, TAG_ANG)
  If oSubNodes.Length > 0 Then
    Set oSubElement = oSubNodes.Item(0)
    dAng = oSubElement.getAttribute(ATTR_A)
  Else
    dAng = 0#
  End If
'set common entity atributes
  oEntity.Rotation = dAng
  oEntity.InsertionPoint = pos
  oEntity.TextString = sText
  oEntity.Height = dSize
End Sub
Private Function getElementsByTagName(oElement As IXMLDOMElement, strTag As String) As IXMLDOMNodeList
   Set getElementsByTagName = oElement.selectNodes("./*[local-name()='" & strTag & "']")
End Function
Private Function getElementByTagName(oElement As IXMLDOMElement, strTag As String) As IXMLDOMElement
   Set getElementByTagName = getElementsByTagName(oElement, strTag).Item(0)
End Function
Public Function CreateText(oElement As IXMLDOMElement, strClass As String) As AcadEntity
Dim oSubNodes As IXMLDOMNodeList
Dim oSubElement As IXMLDOMElement
Dim pos(0 To 2) As Double
Dim dSize As Double
Dim dWrapLen As Double
Dim sText As String
Dim dAng As Double
Dim oText As AcadText
Dim oMText As AcadMText
'find start
  'Set oSubElement = oElement.getElementsByTagName(TAG_START).Item(0)
  Set oSubElement = getElementByTagName(oElement, TAG_START)
  pos(0) = oSubElement.getAttribute(ATTR_X)
  pos(1) = oSubElement.getAttribute(ATTR_Y)
  pos(2) = 0#
'find text size
 ' Set oSubElement = oElement.getElementsByTagName(TAG_SIZE).Item(0)
  Set oSubElement = getElementByTagName(oElement, TAG_SIZE)
  dSize = oSubElement.getAttribute(ATTR_H)
  If strClass = CLS_MTXT Then
    dWrapLen = oSubElement.getAttribute(ATTR_W)
  End If
'find text
'  Set oSubElement = oElement.getElementsByTagName(TAG_CAPTION).Item(0)
  Set oSubElement = getElementByTagName(oElement, TAG_CAPTION)
  sText = oSubElement.text
'find angle
  'Set oSubNodes = oElement.getElementsByTagName(TAG_ANG)
  Set oSubNodes = getElementsByTagName(oElement, TAG_ANG)
  If oSubNodes.Length > 0 Then
    Set oSubElement = oSubNodes.Item(0)
    dAng = oSubElement.getAttribute(ATTR_A)
  Else
    dAng = 0#
  End If
  If strClass = CLS_TXT Then
    Set oText = goModel.AddText(sText, pos, dSize)
    Set CreateText = oText
    oText.ScaleFactor = gdWidthFactor
  Else
    Set oMText = goModel.AddMText(pos, dWrapLen, sText)
    oMText.Height = dSize
    Set CreateText = oMText
  End If
  CreateText.Rotation = dAng
End Function
Public Sub UpdateCircle(oEntity As AcadEntity, oElement As IXMLDOMElement, strClass As String)
Dim oPosition As IXMLDOMElement
Dim oCircle As AcadCircle
Dim oArc As AcadArc
Dim oPt As acadPoint
Dim center(0 To 2) As Double
Dim dRadius As Double
Dim dAlpha As Double
  Set oPosition = getElementByTagName(oElement, TAG_START)
  center(0) = oPosition.getAttribute(ATTR_X)
  center(1) = oPosition.getAttribute(ATTR_Y)
  center(2) = 0#
  If strClass = CLS_PT Then
   Set oPt = oEntity
   oPt.Coordinates = center
  Else
   Set oPosition = getElementByTagName(oElement, TAG_SIZE)
   dRadius = oPosition.getAttribute(ATTR_H)
   If strClass = CLS_CIR Then
    Set oCircle = oEntity
    oCircle.center = center
   Else
    Set oArc = oEntity
  ' need to find arc angles
    Set oPosition = getElementByTagName(oElement, TAG_ANG)
    dAlpha = oPosition.getAttribute(ATTR_A)
    oArc.StartAngle = dAlpha
    dAlpha = oPosition.getAttribute(ATTR_B)
    oArc.EndAngle = dAlpha
   End If
  End If
End Sub

Public Function CreateCircle(oElement As IXMLDOMElement, strClass As String) As AcadEntity
Dim oPosition As IXMLDOMElement
Dim oCircle As AcadCircle
Dim oArc As AcadArc
Dim oPt As acadPoint
Dim center(0 To 2) As Double
Dim dRadius As Double
Dim dAlpha As Double
Dim dBeta As Double
'find center
  'Set oPosition = oElement.getElementsByTagName(TAG_START).Item(0)
  Set oPosition = getElementByTagName(oElement, TAG_START)
  center(0) = oPosition.getAttribute(ATTR_X)
  center(1) = oPosition.getAttribute(ATTR_Y)
  center(2) = 0#
  If strClass = CLS_PT Then
   Set oPt = goModel.AddPoint(center)
   Set CreateCircle = oPt
  Else
   ' find radius
   'Set oPosition = oElement.getElementsByTagName(TAG_SIZE).Item(0)
   Set oPosition = getElementByTagName(oElement, TAG_SIZE)
   dRadius = oPosition.getAttribute(ATTR_H)
   If strClass = CLS_CIR Then
    Set oCircle = goModel.AddCircle(center, dRadius)
    Set CreateCircle = oCircle
   Else
  ' need to find arc angles
    Set oPosition = getElementByTagName(oElement, TAG_ANG)
    'Set oPosition = oElement.getElementsByTagName(TAG_ANG).Item(0)
    dAlpha = oPosition.getAttribute(ATTR_A)
    dBeta = oPosition.getAttribute(ATTR_B)
    Set oArc = goModel.AddArc(center, dRadius, dAlpha, dBeta)
    Set CreateCircle = oArc
   End If
  End If
End Function
Public Sub UpdateLine(oEntity As AcadEntity, oElement As IXMLDOMElement)
Dim oLine As AcadLine
Dim oPoint As IXMLDOMElement
Dim acPoint(0 To 2) As Double
 Set oLine = oEntity
 Set oPoint = getElementByTagName(oElement, TAG_START)
 acPoint(0) = oPoint.getAttribute(ATTR_X)
 acPoint(1) = oPoint.getAttribute(ATTR_Y)
 acPoint(2) = 0#
 oLine.startPoint = acPoint
 Set oPoint = getElementByTagName(oElement, TAG_END)
 acPoint(0) = oPoint.getAttribute(ATTR_X)
 acPoint(1) = oPoint.getAttribute(ATTR_Y)
 acPoint(2) = 0#
 oLine.endPoint = acPoint
End Sub
Public Sub UpdateBlock(oEntity As AcadEntity, oElement As IXMLDOMElement)
Dim oBlock As AcadBlock
Dim oPoint As IXMLDOMElement
Dim acPoint(0 To 2) As Double
 Set oBlock = oEntity
 Set oPoint = getElementByTagName(oElement, TAG_START)
 acPoint(0) = oPoint.getAttribute(ATTR_X)
 acPoint(1) = oPoint.getAttribute(ATTR_Y)
 acPoint(2) = 0#
 oBlock.Origin = acPoint
End Sub

Public Function CreateBlock(oElement As IXMLDOMElement) As AcadEntity
Dim oPoint As IXMLDOMElement
Dim oBlock As AcadBlock
Dim startPoint(0 To 2) As Double
Dim sName As String
  sName = oElement.getAttribute(ATTR_NAME)
' find start point
  Set oPoint = getElementByTagName(oElement, TAG_START)
  startPoint(0) = oPoint.getAttribute(ATTR_X)
  startPoint(1) = oPoint.getAttribute(ATTR_Y)
  startPoint(2) = 0#
  Set oBlock = goModel.InsertBlock(startPoint, sName, 0#, 0#, 0#, 0#)
  Set CreateBlock = oBlock
End Function
Public Function CreateRefBlock(oElement As IXMLDOMElement) As AcadEntity
Dim oPoint As IXMLDOMElement
Dim oAttributes As IXMLDOMNodeList
Dim oEntries As IXMLDOMNodeList
Dim oBlock As AcadBlockReference
Dim startPoint(0 To 2) As Double
Dim sName As String
Dim I As Integer
Dim J As Integer
Dim sTag As String
Dim sKey As String
Dim sVal As String
Dim varAttributes As Variant
  sName = oElement.getAttribute(ATTR_NAME)
' find start point
  Set oPoint = getElementByTagName(oElement, TAG_START)
  startPoint(0) = oPoint.getAttribute(ATTR_X)
  startPoint(1) = oPoint.getAttribute(ATTR_Y)
  startPoint(2) = 0#
  
  Set oBlock = goModel.InsertBlock(startPoint, sName, 1#, 1#, 1#, 0)
  Set oAttributes = getElementsByTagName(oElement, TAG_BLOCK_ATTRIB)
  If oAttributes.Length > 0 Then
    Set oEntries = getElementsByTagName(oAttributes.Item(0), TAG_ENTRY)
    varAttributes = oBlock.GetAttributes
    'iterate over block attributes
    For I = LBound(varAttributes) To UBound(varAttributes)
      sTag = varAttributes(I).TagString
      ' locate block tag in dom and update the value
        For J = 0 To oEntries.Length - 1
          sKey = getElementByTagName(oEntries.Item(J), TAG_KEY).text
          If UCase(sKey) = sTag Then
            sVal = getElementByTagName(oEntries.Item(J), TAG_VALUE).text
            varAttributes(I).TextString = sVal
            Exit For
          End If
        Next
    Next
  End If
  Set CreateRefBlock = oBlock
End Function
Public Function CreateLine(oElement As IXMLDOMElement) As AcadEntity
Dim oPoint As IXMLDOMElement
Dim oLine As AcadLine
Dim startPoint(0 To 2) As Double
Dim endPoint(0 To 2) As Double

' find start point
  Set oPoint = getElementByTagName(oElement, TAG_START)
  startPoint(0) = oPoint.getAttribute(ATTR_X)
  startPoint(1) = oPoint.getAttribute(ATTR_Y)
  startPoint(2) = 0#
' find end point
  Set oPoint = getElementByTagName(oElement, TAG_END)
  endPoint(0) = oPoint.getAttribute(ATTR_X)
  endPoint(1) = oPoint.getAttribute(ATTR_Y)
  endPoint(2) = 0#

' Create the line in model space
  Set oLine = goModel.AddLine(startPoint, endPoint)
  ' set some more ..
  Set CreateLine = oLine
End Function
Public Sub ImportAll()
  Dim oElement As IXMLDOMElement
  Call goDoc.Load(gsDwgDir & "\" & gsDwgName & "_imp.xml")
  For Each oElement In goDoc.documentElement.getElementsByTagName(TAG_ENTITY)
    'Call CreateEntity(oElement)
    Call LoadEntity(oElement)
  Next oElement
End Sub
Public Sub importDoc(doc As MSXML2.DOMDocument)
  Dim oElement As IXMLDOMElement
  For Each oElement In doc.documentElement.getElementsByTagName(TAG_ENTITY)
    Call LoadEntity(oElement)
  Next oElement
End Sub
Public Sub importRaisers(id As Integer)
    Call showArea(id, True, False, False)
End Sub
Public Sub importCenters(id As Integer)
    Call showArea(id, False, True, False)
End Sub
Public Function raiserCount(id As Integer) As Integer
Dim oNodeList As MSXML2.IXMLDOMNodeList
Dim sXpath As String
Dim oArea As IXMLDOMElement
  sXpath = "./" & TAG_AREA_LIST & "/" & TAG_AREA & "[@" & ATTR_ID & "=""" & id & """]"
  Set oNodeList = goStatus.documentElement.selectNodes(sXpath)
  If oNodeList.Length = 0 Then
    raiserCount = -1
  Else
    Set oArea = oNodeList.Item(0)
    Set oNodeList = oArea.getElementsByTagName(TAG_RAISER_LIST)
    raiserCount = oNodeList.Length
  End If
End Function
Public Sub colorEdges(areaId As Integer)
    Call showArea(areaId, False, False, True)
End Sub
Public Sub showArea(id As Integer, showRaiser As Boolean, showCenter As Boolean, colorEdges As Boolean)
Dim oNodeList As MSXML2.IXMLDOMNodeList
Dim sXpath As String
Dim oArea As IXMLDOMElement
Dim oList As IXMLDOMElement
Dim oElement As IXMLDOMElement
Dim nId As Long
Dim errAcColor As Integer
  sXpath = "./" & TAG_AREA_LIST & "/" & TAG_AREA & "[@" & ATTR_ID & "=""" & id & """]"
  Set oNodeList = goStatus.documentElement.selectNodes(sXpath)
  If oNodeList.Length = 0 Then
    Exit Sub
  End If
  Set oArea = oNodeList.Item(0)
  If colorEdges Then
    Set oNodeList = oArea.getElementsByTagName(TAG_EDGE_ID_LIST)
    If oNodeList.Length > 0 Then
        errAcColor = acColorCode(getErrorColor())
        Set oList = oNodeList.Item(0)
        For Each oElement In oList.getElementsByTagName(TAG_ID)
          nId = oElement.text
          Call setColor(nId, errAcColor)
        Next oElement
    End If
  End If
  If showRaiser Then
    Set oNodeList = oArea.getElementsByTagName(TAG_RAISER_LIST)
    If oNodeList.Length > 0 Then
        Set oList = oNodeList.Item(0)
        For Each oElement In oList.getElementsByTagName(TAG_RAISER)
          Call LoadEntity(oElement)
        Next oElement
    End If
  End If
  If showCenter Then
    Set oNodeList = oArea.getElementsByTagName(TAG_AREA_CENTER)
    Set oElement = oNodeList.Item(0)
    Call LoadEntity(oElement)
  End If
  ThisDrawing.Application.Update
End Sub
Public Sub importEntityErrors()
  Dim oList As IXMLDOMElement
  Dim oElement As IXMLDOMElement
  Dim oNodeList As MSXML2.IXMLDOMNodeList
  Set oNodeList = status.documentElement.getElementsByTagName(TAG_ERR_LIST)
  If oNodeList.Length = 0 Then
    Exit Sub
  End If
  Set oList = oNodeList.Item(0)
  For Each oElement In oList.getElementsByTagName(TAG_ERR_ENTITY)
    Call LoadEntity(oElement)
  Next oElement
  ThisDrawing.Application.Update
End Sub
Public Sub LoadEntity(oElement As IXMLDOMElement)
Dim nId As Long
Dim oEntity As AcadEntity
  If IsNull(oElement.getAttribute(ATTR_ID)) Then
   nId = 0
  Else
   nId = oElement.getAttribute(ATTR_ID)
  End If
  If nId = 0 Then
   Call CreateEntity(oElement)
  Else
'use this syntax for 32bit
   Set oEntity = goAutoCAD.ActiveDocument.ObjectIdToObject(nId)
'use this syntax for 64bit
'   Set oEntity = goAutoCAD.ActiveDocument.ObjectIdToObject32(nId)
   Call UpdateEntity(oEntity, oElement)
 End If
End Sub
Public Sub setColor(nId As Long, color As Integer)
Dim oEntity As AcadEntity
'use this syntax for 32bit
   Set oEntity = goAutoCAD.ActiveDocument.ObjectIdToObject(nId)
'use this syntax for 64bit
'   Set oEntity = goAutoCAD.ActiveDocument.ObjectIdToObject32(nId)
   oEntity.color = color
End Sub

Public Sub UpdateEntity(oEntity As AcadEntity, oElement As IXMLDOMElement)
Dim sClassName As String
Dim sLayer As Variant
Dim sColor As Variant
  sLayer = oElement.getAttribute(ATTR_LAYER)
  If Not IsNull(sLayer) Then
        oEntity.Layer = sLayer
  End If
  sColor = oElement.getAttribute(ATTR_COLOR)
  If Not IsNull(sColor) Then
        oEntity.color = acColorCode(sColor)
  End If
  sClassName = oEntity.ObjectName
   If sClassName = CLS_LINE Then
      Call UpdateLine(oEntity, oElement)
   ElseIf sClassName = CLS_CIR Or sClassName = CLS_ARC Or sClassName = CLS_PT Then
      Call UpdateCircle(oEntity, oElement, sClassName)
   ElseIf sClassName = CLS_TXT Or sClassName = CLS_MTXT Then
      Call UpdateText(oEntity, oElement, sClassName)
   ElseIf sClassName = CLS_BLK Then
      Call UpdateBlock(oEntity, oElement)
   End If
End Sub
Public Sub CreateEntity(oElement As IXMLDOMElement)
Dim sClassName As String
Dim sLayer As Variant
Dim sColor As Variant
Dim oEntity As AcadEntity
Dim bValid As Boolean
   bValid = True
   sClassName = oElement.getAttribute(ATTR_CLS)
   If sClassName = CLS_LINE Then
      Set oEntity = CreateLine(oElement)
   ElseIf sClassName = CLS_CIR Or sClassName = CLS_ARC Or sClassName = CLS_PT Then
      Set oEntity = CreateCircle(oElement, sClassName)
   ElseIf sClassName = CLS_TXT Or sClassName = CLS_MTXT Then
      Set oEntity = CreateText(oElement, sClassName)
   ElseIf sClassName = CLS_BLK Then
      Set oEntity = CreateBlock(oElement)
   ElseIf sClassName = CLS_REFBLK Then
      Set oEntity = CreateRefBlock(oElement)
   Else
     bValid = False
   End If
   If bValid Then
     sLayer = oElement.getAttribute(ATTR_LAYER)
     If Not IsNull(sLayer) Then
        oEntity.Layer = sLayer
     End If
     sColor = oElement.getAttribute(ATTR_COLOR)
     If Not IsNull(sColor) Then
        oEntity.color = acColorCode(sColor)
     End If
   End If
End Sub
Public Function UpdateAreaInfo(oBlock As IXMLDOMElement, oArea As IXMLDOMElement) As Integer
'Dim vRet As Variant
'Dim sRest As String
'Dim sTok As String
Dim sTag As String
Dim sVal As String
Dim oElement As IXMLDOMElement
Dim oOpt As IXMLDOMElement
Dim oEntries As IXMLDOMNodeList
Dim oEntry As IXMLDOMElement
Dim I As Integer
  UpdateAreaInfo = 0
  If oBlock.getAttribute(ATTR_CLS) = CLS_REFBLK And oBlock.getAttribute(ATTR_NAME) = ONAME_AREA_CONFIG Then
      Set oOpt = oArea.getElementsByTagName(ENTITY_OPT_ROOT).Item(0)
      UpdateAreaInfo = 1
      Set oEntries = findMapEntries(oBlock, TAG_BLOCK_ATTRIB)
      For I = 0 To oEntries.Length - 1
        Set oEntry = oEntries.Item(I)
        sVal = findEntryValue(oEntry)
        sTag = findEntryKey(oEntry)
        Set oElement = createDocElement(LCase(sTag), LTrim(RTrim(sVal)))
        If sTag = ONAME_AREA_ID Or sTag = ONAME_AREA_NAME Then
          Call oArea.appendChild(oElement)
        Else
          Call oOpt.appendChild(oElement)
        End If
      Next
  End If
End Function
Public Function Entity2Element(oEntity As AcadEntity) As IXMLDOMElement
Dim oElement As IXMLDOMElement
Dim oSubElement As IXMLDOMElement
Dim oAttributes As IXMLDOMElement
Dim oEntry As IXMLDOMElement
Dim oPoint As Variant
Dim vAttributes As Variant
Dim sName As String
Dim sVal As String
Dim I As Integer

  Set oElement = createDocElement(TAG_ENTITY)
  Call oElement.setAttribute(ATTR_CLS, oEntity.ObjectName)
  
  'use this syntax for 32bit
  Call oElement.setAttribute(ATTR_ID, oEntity.ObjectID)
  'use this syntax for 64bit
'  Call oElement.setAttribute(ATTR_ID, oEntity.ObjectID32)

  Call oElement.setAttribute(ATTR_LAYER, oEntity.Layer)
  If oEntity.ObjectName = CLS_LINE Then
   oPoint = oEntity.startPoint
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
   
   oPoint = oEntity.endPoint
   Set oSubElement = createDocElement(TAG_END)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
  ElseIf oEntity.ObjectName = CLS_PT Then
   oPoint = oEntity.Coordinates
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
  ElseIf oEntity.ObjectName = CLS_CIR Then
   Set oSubElement = createDocElement(TAG_SIZE)
   Call oSubElement.setAttribute(ATTR_H, oEntity.Radius)
   Call oElement.appendChild(oSubElement)
   
   oPoint = oEntity.center
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
  ElseIf oEntity.ObjectName = CLS_ARC Then
   Set oSubElement = createDocElement(TAG_SIZE)
   Call oSubElement.setAttribute(ATTR_H, oEntity.Radius)
   Call oElement.appendChild(oSubElement)
   
   oPoint = oEntity.center
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
   
   Set oSubElement = createDocElement(TAG_ANG)
   Call oSubElement.setAttribute(ATTR_A, oEntity.StartAngle)
   Call oSubElement.setAttribute(ATTR_B, oEntity.EndAngle)
   Call oElement.appendChild(oSubElement)
   
  ElseIf oEntity.ObjectName = CLS_MTXT Or oEntity.ObjectName = CLS_TXT Then
   Set oSubElement = createDocElement(TAG_SIZE)
   Call oSubElement.setAttribute(ATTR_H, oEntity.Height)
   If oEntity.ObjectName = CLS_MTXT Then
      Call oSubElement.setAttribute(ATTR_W, oEntity.Width)
   End If
   Call oElement.appendChild(oSubElement)
   
   oPoint = oEntity.InsertionPoint
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
   
   Set oSubElement = createDocElement(TAG_ANG)
   Call oSubElement.setAttribute(ATTR_A, oEntity.Rotation)
   Call oElement.appendChild(oSubElement)
   
   Set oSubElement = createDocElement(TAG_CAPTION, oEntity.TextString)
   Call oElement.appendChild(oSubElement)
  ElseIf oEntity.ObjectName = CLS_BLK Then
  
   Call oElement.setAttribute(ATTR_NAME, oEntity.name)
   oPoint = oEntity.Origin
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
  ElseIf oEntity.ObjectName = CLS_REFBLK Then
 ' Set blockRef = oEntity
   Call oElement.setAttribute(ATTR_NAME, oEntity.name)
   oPoint = oEntity.InsertionPoint
   Set oSubElement = createDocElement(TAG_START)
   Call oSubElement.setAttribute(ATTR_X, oPoint(0))
   Call oSubElement.setAttribute(ATTR_Y, oPoint(1))
   Call oElement.appendChild(oSubElement)
   If oEntity.HasAttributes Then
     Set oAttributes = createDocElement(TAG_BLOCK_ATTRIB)
     Call oElement.appendChild(oAttributes)
     vAttributes = oEntity.GetAttributes
    'iterate over block attributes
     For I = LBound(vAttributes) To UBound(vAttributes)
      sName = vAttributes(I).TagString
      sVal = vAttributes(I).TextString
      Set oEntry = createMapEntry(goDoc, sName, sVal)
      Call oAttributes.appendChild(oEntry)
     Next
   End If
  End If
  Set Entity2Element = oElement
End Function
Public Function AppendCollection(oRoot As IXMLDOMNode, oCollection As Object) As Boolean
  Dim oEntity As AcadEntity
  Dim oNode As IXMLDOMNode
  Dim nIdCnt As Integer
  nIdCnt = 0
  For Each oEntity In oCollection
  
    Set oNode = Entity2Element(oEntity)
    Call oRoot.appendChild(oNode)
    nIdCnt = nIdCnt + UpdateAreaInfo(oNode, oRoot.parentNode)
  Next oEntity
  If nIdCnt > 1 Then
      Call MsgBox("Only one area id can be selected")
      AppendCollection = False
  Else
      AppendCollection = True
  End If
End Function
Public Sub ExtractAll()
    Dim stat As Boolean
    Call initDwgDoc

    stat = AppendCollection(goDoc.documentElement.getElementsByTagName(ENTITY_ROOT).Item(0), goModel)
    goDoc.Save (gsDwgDir & "\" & gsDwgName & ".xml")
End Sub
Public Function ExtractSelection() As Boolean
    Dim sset As AcadSelectionSet
    For Each sset In goAutoCAD.ActiveDocument.SelectionSets
      If sset.name = ONAME_EXTRACT Then
        sset.Delete
      End If
    Next sset
    Set sset = goAutoCAD.ActiveDocument.SelectionSets.Add(ONAME_EXTRACT)
    MsgBox "Select desired portion in AutoCAD and press ENTER"
    ' Prompt the user to select objects
    ' and add them to the selection set.
    ' To finish selecting, press ENTER.
    sset.SelectOnScreen
    'create root
    Call initDwgDoc

    ExtractSelection = AppendCollection(goDoc.documentElement.getElementsByTagName(ENTITY_ROOT).Item(0), sset)
    goDoc.Save (gsDwgDir & "\" & gsDwgName & ".xml")
    sset.Delete
End Function
Private Sub initDwgDoc()
Dim oBody As IXMLDOMNode
Dim oOpt As IXMLDOMNode
Dim oNode As IXMLDOMNode
Dim id As Long
 
  Call goDoc.setProperty("SelectionLanguage", "XPath")
  Set goDoc.documentElement = createDocElement(DWG_NS_PREFIX & ":" & TAG_AREA_ROOT)
  id = goConf.getElementsByTagName(TAG_DWG_ID).Item(0).text
  Set oNode = createDocElement(TAG_DWG_ID, id)
  Call goDoc.documentElement.appendChild(oNode)
  Set oBody = createDocElement(ENTITY_ROOT)
  Call goDoc.documentElement.appendChild(oBody)
  Set oOpt = createDocElement(ENTITY_OPT_ROOT)
  Call goDoc.documentElement.appendChild(oOpt)
End Sub
Private Sub initCfgDoc()
 Call goConf.setProperty("SelectionLanguage", "XPath")
 Call initDoc(goConf, TAG_ROOT)
End Sub
Public Function createRenderingReq(id As Long) As MSXML2.DOMDocument
Dim oNodes As IXMLDOMNodeList
Dim oHead As IXMLDOMNode
Dim oNode As IXMLDOMNode
    Set createRenderingReq = New MSXML2.DOMDocument
    Set createRenderingReq.documentElement = createElement(createRenderingReq, DWG_NS_PREFIX & ":" & TAG_RENDER_DRAWING)
    Set oHead = createRenderingReq.documentElement
    Set oNodes = getElementsByTagName(goConf.documentElement, TAG_DWG_ID)
    If oNodes.Length > 0 Then
      Set oNode = createElement(createRenderingReq, TAG_DWG_ID, oNodes.Item(0).text)
      Call oHead.appendChild(oNode)
    End If
    Set oNode = createElement(createRenderingReq, TAG_AREA_ID, id)
    Call oHead.appendChild(oNode)
End Function
Public Function createStatusReq() As MSXML2.DOMDocument
  Set createStatusReq = New MSXML2.DOMDocument
  Call initDoc(createStatusReq, TAG_GET_STATUS)
End Function
Public Sub renderBranchLabels(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_BRANCH_LABELS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderMainLabels(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_MAIN_LABELS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderMainCuts(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_MAIN_CUTS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderBranchCuts(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_BRANCH_CUTS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderMainDiam(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_MAIN_DIAMS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderBranchDiam(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_BRANCH_DIAMS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderMainSpan(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_MAIN_SPANS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderBranchSpan(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_BRANCH_SPANS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Public Sub renderHeads(doc As MSXML2.DOMDocument)
Dim oNode As IXMLDOMNode
   Set oNode = createElement(doc, TAG_BRANCH_HEADS, "true")
   Call doc.documentElement.appendChild(oNode)
End Sub
Private Sub initDoc(doc As MSXML2.DOMDocument, rootTag As String)
' Loads an empty request/config document with name, ts and font from captured acad globals
Dim oHead As IXMLDOMNode
Dim oNode As IXMLDOMNode

    Call doc.setProperty("SelectionLanguage", "XPath")

    Set doc.documentElement = createElement(doc, DWG_NS_PREFIX & ":" & rootTag)
    Set oHead = doc.documentElement
    Set oNode = createElement(doc, TAG_NAME, gsDwgName)
    Call oHead.appendChild(oNode)
    
    Set oNode = createElement(doc, TAG_TEXT_SIZE, gdTextSize)
    Call oHead.appendChild(oNode)
    Set oNode = createElement(doc, TAG_UPD_DATE, gsUpdateDate)
    Call oHead.appendChild(oNode)
End Sub
Public Sub ExtractConfiguration()
    goConf.Save (gsDwgDir & "\" & gsDwgName & "_cfg.xml")
End Sub
Public Function NextLayerType(sType As String) As String
Dim outNodes As IXMLDOMNodeList
Set outNodes = FindLayerNodesByType(LAYER_OUT)
If sType = "" Then
    NextLayerType = LAYER_NONE
ElseIf sType = LAYER_NONE Then
    If outNodes.Length = 0 Then
        NextLayerType = LAYER_OUT
    Else
        NextLayerType = LAYER_MAIN
    End If
ElseIf sType = LAYER_OUT Then
    NextLayerType = LAYER_NONE
ElseIf sType = LAYER_MAIN Then
    NextLayerType = LAYER_BRANCH
ElseIf sType = LAYER_BRANCH Then
    NextLayerType = LAYER_NONE
End If
End Function
Public Function nextVendor(sType As String, sVend As String) As String
If sType = SUB_TYPE_THREADED Then
  nextVendor = ""
ElseIf sVend = VEND_VIC Then
  nextVendor = VEND_FIRELOCK
Else
  nextVendor = VEND_VIC
End If
End Function
Public Function nextMainSubType(sType As String) As String
If sType = SUB_TYPE_GROOVED Then
  nextMainSubType = SUB_TYPE_WELDED
ElseIf sType = SUB_TYPE_WELDED Then
    nextMainSubType = SUB_TYPE_WELDED_GROOVE
ElseIf sType = SUB_TYPE_WELDED_GROOVE Then
  nextMainSubType = SUB_TYPE_THREADED
Else
  nextMainSubType = SUB_TYPE_GROOVED
End If
End Function
Public Property Get configuration() As Variant
 Set configuration = goConf
End Property

Public Property Get drawing() As Variant
 Set drawing = goDoc
End Property
Public Sub renderStatus(area As MSXML2.DOMDocument)
 Dim oEntityRoot As IXMLDOMElement
 Dim I As Integer
 Dim oElement As IXMLDOMElement
 
 Dim oNodeList As MSXML2.IXMLDOMNodeList
  ' Set oEntityRoot = area.documentElement.selectNodes("./*[local-name()='" & TAG_RENDERING & "']").Item(0)
   Set oEntityRoot = getElementByTagName(area.documentElement, TAG_RENDERING)
   Set oNodeList = oEntityRoot.childNodes
   For I = 0 To oNodeList.Length - 1
    Set oElement = oNodeList.Item(I)
    Call LoadEntity(oElement)
   Next
End Sub
Public Sub loadStatus(oLst As ListBox, oBtn As Control)
  Dim oList As IXMLDOMElement
  Dim oElement As IXMLDOMElement
  Dim oNodeList As MSXML2.IXMLDOMNodeList
  Dim id As Integer
  Dim areaStatus As IXMLDOMElement
  Dim I As Integer
  Dim bStat As Boolean
  Dim sMsg As String
  Dim nCnt As Integer
  If goStatus.childNodes.Length = 0 Then
    Exit Sub ' never set
  End If
  Set oNodeList = goStatus.documentElement.getElementsByTagName(TAG_ERR_LIST)
  If oNodeList.Length > 0 Then
      Set oList = oNodeList.Item(0)
      nCnt = oList.childNodes.Length
      If nCnt = 0 Then
        oBtn.Caption = "No Errors"
      Else
        oBtn.Caption = "Errors(" & nCnt & ")"
      End If
  End If
  ' process area map
  Set oNodeList = goStatus.documentElement.selectNodes("./*[local-name()='" & TAG_AREA & "']")
  If oNodeList.Length = 0 Then
    Exit Sub
  End If
  Set oList = oNodeList.Item(0)
  Set oNodeList = oList.getElementsByTagName(TAG_ENTRY)
  oLst.Clear
  For I = 0 To oNodeList.Length - 1
    Set oElement = oNodeList.Item(I)
    id = oElement.getElementsByTagName(TAG_KEY).Item(0).text
    Set areaStatus = oElement.getElementsByTagName(TAG_VALUE).Item(0)
    Call oLst.AddItem(id, I)
    oLst.Column(1, I) = getElementByTagName(areaStatus, TAG_AREA_NAME).text & ":" & getElementByTagName(areaStatus, TAG_AREA_READINESS).text
  Next
End Sub


Public Property Get status() As Object
 Set status = goStatus
End Property

Public Property Let status(vNewValue As Object)
Dim id As Long
  Set goStatus = vNewValue
  id = goStatus.documentElement.selectNodes("./*[local-name()='" & TAG_DWG_ID & "']").Item(0).text
  Call updateHeaderProp(TAG_DWG_ID, id)
End Property
Private Function acColorCode(colorTxt As Variant) As Integer
Dim ret As Integer
If colorTxt = "Magenta" Then
  ret = acMagenta
ElseIf colorTxt = "Cyan" Then
  ret = acCyan
Else
  ret = acWhite
End If
acColorCode = ret
End Function

Public Sub setWeldIfEq(bVal As Boolean)
Dim sVal As String
sVal = "N"
If bVal Then
    sVal = "Y"
End If
    Call setOption(TAG_WELD_IF_EQ, sVal)
End Sub

Public Function getWeldIfEq() As Boolean
Dim sVal As String
Dim ret As Boolean
    sVal = getOption(TAG_WELD_IF_EQ, "Y")
    ret = False
    If sVal = "Y" Then
        ret = True
    End If
    getWeldIfEq = ret
End Function
