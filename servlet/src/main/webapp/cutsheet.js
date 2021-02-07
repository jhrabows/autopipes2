function CutSheet(type, dwgId, areaId, rtl) {
//console.info('CutSheet(' + type + ',' + layer + ',' + dwgId + ',' + areaId + ')');
  this.currentDwg = null;
  this.type = type;
  this.rtl = rtl;
  this.id = dwgId;
  this.areaId = areaId;
  
  this.processCutSheet();
}

CutSheet.prototype.fractal = function(decimal){

  var fract = decimal % 1;
  decimal -= fract;
  var ret = '';
  if(decimal > 0){
      ret += decimal;
  }
  if(fract == 0.5){
      ret += '½';
  }else if(fract == 0.25){
      ret += '¼';
  }else if(fract == 0.75){
      ret += '¾';
  }
  return ret;
};

CutSheet.prototype.fractalSequence = function(decimals){
  var ret = '';
  if(decimals){
	  for(var i = 0; i < decimals.length; i++){
	    if(i > 0){
	      ret += 'x';
	    }
	    ret += (this.fractal(decimals[i]) + '"');
	  }
  }
  return ret;
};

CutSheet.prototype.footInch = function(decimal){
  var fract = decimal % 12,
    fract_rest = fract % 1,
  	fract_whole = fract - fract_rest;
  decimal -= fract;
  decimal /= 12;
  return '' + decimal + '\'-' + fract_whole + (fract_rest > 0 ? this.fractal(fract_rest): '') + '"';
};

CutSheet.prototype.formatFitting = function(endFitting){
    return this.fractalSequence(endFitting.diameterList) + ' '
        + this.formatFittingType(endFitting.type, endFitting.diameterList.length).toUpperCase();
};

CutSheet.prototype.formatFittingType = function(fittingType, numOfDiameters){
    if(fittingType == 'Coupling'){
        return numOfDiameters == 2 ? 'Reducer' : 'Coupling';
    }
    return fittingType;
};

CutSheet.prototype.zeroPad = function( str, count) {
	var l;
	for ( l = str.length; l < count; l += 1 ) {
		str =  ("0" + str);
	}
	return str;
};

CutSheet.prototype.normalizeOutlets = function(outlets){
	var ret = [],
		ol = outlets.length;
	
	for(var i = 0; i < ol; i++){
		var outlet = outlets[i],
			diameter = outlet.diameter,
			offset = outlet.offset;
		if(outlet.jumpLocation === 'MIDDLE'){
			ret.push({diameter: diameter, offset: offset, jumpLocation: 'TOP', sideCount: 0});
			ret.push({diameter: diameter, offset: offset, jumpLocation: 'BOTTOM', sideCount: 0});
		}else if(outlet.sideCount === 2){
			ret.push({diameter: diameter, offset: offset, jumpLocation: 'NONE', sideCount: 1});
			ret.push({diameter: diameter, offset: offset, jumpLocation: 'NONE', sideCount: -1});
		}else{
			ret.push(outlet);
		}
	}
	return ret;
};

CutSheet.prototype.locationCode = function(jumpPos, sideCnt){
	var code = '';
	if(jumpPos === 'TOP'){
		code = 'C';
	}else if(jumpPos === 'BOTTOM'){
		code = 'A';
	}else if(jumpPos === 'MIDDLE'){
		code = 'AC';
	}else{
		if(sideCnt == 1){
			code = 'B';
		}else if(sideCnt == -1){
			code = 'D';
		}else if(sideCnt == 2){
			code = 'BD';
		}
	}
	return code;
};

CutSheet.prototype.outletType = function(attachment){
	var ret = '';
	if(attachment){
		ret = (attachment === 'weldedGroove' ? 'GR' : 'TH');
	}
	return ret;
};

CutSheet.prototype.displayGrRow = function(tabId, info, idSize){
    dwr.util.cloneNode("cutpattern", { idSuffix:tabId });
    dwr.util.setValue("tab-tag" + tabId, info.prefix + this.zeroPad(String(info.id), idSize));
    dwr.util.setValue("tab-count" + tabId, 1); // always 1 for now
    dwr.util.setValue("tab-size" + tabId, this.fractal(info.diameter) + '"');
    dwr.util.setValue("tab-length" + tabId, info.cutLength ? this.footInch(info.cutLength) : '');
    if(this.type === 'WDM'){
        dwr.util.setValue("tab-ends" + tabId, 'GRxGR');
    }
    var outlets = (this.rtl === 'Y') ? info.outletsRTL : info.outlets;
    
//    outlets = this.normalizeOutlets(outlets);
    
    var outletCount = outlets ? outlets.length : 0;
    if(this.type === 'WDM' && outletCount > 4 || this.type === 'GRM' && outletCount > 5){
        dwr.util.cloneNode("cutpattern2", { idSuffix:tabId }); // TODO: only 2 outlet rows supported for now
    }
    for(var i = 0; i < outletCount; i++){
    	var outlet = outlets[i],
    		offsetId = 'tab-o' + (i + 1) + '-offset' + tabId,
    		diameterId = 'tab-o' + (i + 1) + '-diam' + tabId,
    		locationId = 'tab-o' + (i + 1) + '-location' + tabId,
    		typeId = 'tab-o' + (i + 1) + '-type' + tabId;
    	
    	dwr.util.setValue(offsetId, outlet.offset ? this.footInch(outlet.offset) : '');
    	dwr.util.setValue(diameterId, this.fractal(outlet.diameter) + '"');
    	dwr.util.setValue(locationId, this.locationCode(outlet.jumpLocation, outlet.sideCount));
    	if(this.type === 'WDM'){
        	dwr.util.setValue(typeId, this.outletType(outlet.attachment));
    	}
    }
};

CutSheet.prototype.displayRow = function(tabId, info, count, idSize, forMain, originalAttachment){
	var prefix = (forMain ? 'M-' : '#'),
			afterTakeout = forMain ? info.pipe.afterTakeout : Math.round(info.pipe.afterTakeout);
	
    dwr.util.cloneNode("cutpattern", { idSuffix:tabId });
    dwr.util.setValue("tab-tag" + tabId, prefix + this.zeroPad(String(info.pipe.id), idSize));
    dwr.util.setValue("tab-count" + tabId, count);
    dwr.util.setValue("tab-size" + tabId, this.fractal(info.pipe.diameter) + '"');
    dwr.util.setValue("tab-length" + tabId, info.pipe.vertical ? 'JUMP' :  this.footInch(afterTakeout));
    var type = (originalAttachment === 'grooved' || originalAttachment === 'weldedGroove') ? 'THxGR' : '';
    if(info.pipe.endAttachment.directionInFitting == 'N' && info.endFitting.type == 'Tee'){
        type +=  ((type != '' ? '<br/>' : '') + 'BULL');
    }
    document.getElementById("tab-type" + tabId).innerHTML = type;
  //  dwr.util.setValue("tab-type" + tabId, type);
    dwr.util.setValue("tab-fitting" + tabId,
    		this.formatFitting(info.endFitting));
    dwr.util.setValue("tab-span" + tabId, this.footInch(info.pipe.span));
    if(!info.pipe.vertical){
      dwr.util.setValue("tab-beg-takeout" + tabId, info.pipe.startAttachment.takeout);
      dwr.util.setValue("tab-end-takeout" + tabId, info.pipe.endAttachment.takeout);
    }
};

CutSheet.prototype.processCutSheet = function(){
    var self = this;
    if(this.type == 'THM'){
        Autopipes.getMainThreadedList(this.id, this.areaId, displayThCutSheet);
    }else if(this.type == 'BR'){
        Autopipes.getBranchInfoForArea(this.id, this.areaId, displayBrCutSheet);
    }else if(this.type == 'GRM'){
        Autopipes.getMainGroovedList(this.id, this.areaId, displayGrCutSheet);
    }else if(this.type == 'WDM'){
        Autopipes.getMainWeldedList(this.id, this.areaId, displayGrCutSheet);
    }else{
        alert('No cut-sheet for type ' + this.type);
    }
    var info;
    var count;
    var type;
    var tabId;
    function displayBrCutSheet(branchMap){
        dwr.util.removeAllRows("cutbody", { filter:function(tr) {
          return (tr.id != "cutpattern");
        }});
        var idSize = decimalCount(branchMap);
        for(var brId in branchMap){
            var brInfo = branchMap[brId],
            	brCount = brInfo.multiplicity,
            	originalAttachment = brInfo.originalAttachment;
            
            for(var i = 0; i < brInfo.edgeMultiplicity.length; i++){
                info = brInfo.edgeMultiplicity[i].edgeInfo;
                count = brInfo.edgeMultiplicity[i].count;
		        tabId = 'I-' + brId + '-' + (i + 1);
                count *= brCount;
                self.displayRow(tabId, info, count, idSize, false, (i ? '' : originalAttachment));
            }
            dwr.util.cloneNode("cutpattern");
        }
    }    
    
    function displayThCutSheet(infolist){
      dwr.util.removeAllRows("cutbody", { filter:function(tr) {
          return (tr.id != "cutpattern");
        }});
      var idSize = decimalInfoCount(infolist, true);
      for (var i = 0; i < infolist.length; i++) {
         info = infolist[i];
         tabId = 'I' + i;
         self.displayRow(tabId, info, 1, idSize, true, '');
      }
    }
    
    function displayGrCutSheet(infolist){
        dwr.util.removeAllRows("cutbody", { filter:function(tr) {
            return (tr.id !== "cutpattern" && tr.id !== "cutpattern2");
          }});
        var idSize = decimalInfoCount(infolist, false);
        for (var i = 0; i < infolist.length; i++) {
           info = infolist[i];
           tabId = 'I' + i;
           self.displayGrRow(tabId, info, idSize);
        }
      }
    
    function decimalCount(branchMap){
    	var idLen = 0;
    	for(var brId in branchMap){
    		idLen = Math.max(idLen, brId.length);
    	}
    	return idLen;
    }
    
    function decimalInfoCount(infolist, inPipe){
    	var ret = 0;
    	if(infolist.length){
        	var last = infolist[infolist.length - 1],
        		lastId = String(inPipe ? last.pipe.id : last.id);
        	ret = lastId.length;
    	}
    	return ret;
    }
    

};