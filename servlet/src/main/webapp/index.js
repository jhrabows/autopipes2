var g_currentDwg;

function init(){
  dwr.util.removeAllOptions('drawing');
  g_currentDwg = null;
  Storage.findAllDrawings(displayDrawingNames);
  
    function displayDrawingNames(drawings){
        dwr.util.addOptions("drawing", [ { id: 0, dwgName: '-select-'} ], "id", "dwgName");
        dwr.util.addOptions("drawing", drawings, "id", "dwgName");
    }
}

function deleteDrawing(){
    var id = dwr.util.getValue("drawing");
    if(id > 0){
      Storage.deleteDrawing(id, processDeleteDrawing);
    }
    function processDeleteDrawing(){
      init();
    }
}
function deleteArea(){
    var id = dwr.util.getValue("drawing");
    var aid = dwr.util.getValue("area");
    if(id > 0){
      Storage.deleteArea(id, aid, processDeleteArea);
    }
    function processDeleteArea(){
      var id = dwr.util.getValue("drawing");
      Storage.findDrawingAreas(id, displayAreaNames);
    }
}
function processChangeDrawing(){
    var id = dwr.util.getValue("drawing");
//    alert('Selected: [' + id + ']');
    if(id > 0){
      Storage.findDrawingAreas(id, displayAreaNames);
      Storage.findOneDrawing(id, displayDrawing);
    }else{
        dwr.util.removeAllOptions("area");
        dwr.util.removeAllOptions("layer");
    }
    
    function displayDrawing(drawing){
        g_currentDwg = drawing;       
       // var layers = drawing.optionsRoot.layer;
       // dwr.util.addOptions("layer", layers, "name");
    }
}

function displayAreaNames(areas){
    dwr.util.removeAllOptions("area");
    dwr.util.addOptions("area", areas, "areaId", "areaName");
    processChangeArea();
}

function processChangeArea(){
    var id = dwr.util.getValue("drawing");
    var areaId = dwr.util.getValue("area");
//    alert('Selected Area: [' + id + ':' + areaId + ']');
    Storage.findOneDrawingArea(id, areaId, false, false, displayArea);
    
    function displayArea(area){
        dwr.util.setValue("readiness", area.areaReadiness);
    }
}

function processCutSheet(debug){
    var id = dwr.util.getValue("drawing");
    var areaId = dwr.util.getValue("area");
    var selType = dwr.util.getValue("csType");
    var selTypeSplit = selType.split('_');
    var rtl = 'N';
    var csType = selType;
    
    if(selTypeSplit.length > 1){
    	csType = selTypeSplit[0];
    	rtl = selTypeSplit[1];
    }
      
    var href = 'cutsheet.jsp?type=' + csType + '&rtl=' + rtl
       + '&dwgId=' + id + '&areaId=' + areaId + '&debug=' + debug;
    
    window.open(href, 'mywin', 'width=500, height=500, toolbar=1, scrollbars=1, resizable=1');
}    
