<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
  <title>Auto Pipes</title>
  <script type='text/javascript' src='/autopipes/dwr/interface/Storage.js'></script>
  <script type='text/javascript' src='/autopipes/dwr/interface/Autopipes.js'></script>
  <script type='text/javascript' src='/autopipes/dwr/engine.js'></script>
  <script type='text/javascript' src='/autopipes/dwr/util.js'></script>
  <script type="text/javascript" src="cutsheet.js"></script>
</head>
<body
  onload="javascript:new CutSheet('${param['type']}', ${param['dwgId']}, ${param['areaId']}, '${param['rtl']}');">
   <table border="1">
   
<!--Non-Threaded Main -->
   
<c:if test="${param['type'] == 'GRM' || param['type'] == 'WDM'}">
	<thead>
		<tr>
       		<th>MAIN #</th>
       		<th>AMOUNT</th>
       		<th>SIZE</th>
       		<th>LENGTH</th>
       		<th>ENDS</th>
       		
  <c:if test="${param['type'] == 'GRM'}">
     		<th colspan="3">
     			OUTLET #1<br/>SIZE &amp; LOCATION
     		</th>
     		<th colspan="3">
     			OUTLET #2<br/>SIZE &amp; LOCATION
     		</th>
     		<th colspan="3">
     			OUTLET #3<br/>SIZE &amp; LOCATION
     		</th>
     		<th colspan="3">
     			OUTLET #4<br/>SIZE &amp; LOCATION
     		</th>
     		<th colspan="3">
     			OUTLET #5<br/>SIZE &amp; LOCATION
     		</th>
  </c:if>

  <c:if test="${param['type'] == 'WDM'}">
     		<th colspan="4">
     			WELDED OUTLET #1<br/>SIZE, LOCATION  &amp; TYPE
     		</th>
     		<th colspan="4">
     			WELDED OUTLET #2<br/>SIZE, LOCATION  &amp; TYPE
     		</th>
     		<th colspan="4">
     			WELDED OUTLET #3<br/>SIZE, LOCATION  &amp; TYPE
     		</th>
     		<th colspan="4">
     			WELDED OUTLET #4<br/>SIZE, LOCATION  &amp; TYPE
     		</th>

 </c:if>
     		
   		</tr>
   	</thead>
   	<tbody id="cutbody">
   		<tr id="cutpattern">
   			<td><span id="tab-tag"></span></td>
   			<td><span id="tab-count"></span></td>
   			<td><span id="tab-size"></span></td>
   			<td><span id="tab-length"></span></td>
   			<td><span id="tab-ends"></span></td>
   			
   			<td><span id="tab-o1-diam"></span></td>
   			<td><span id="tab-o1-offset"></span></td>
   			<td><span id="tab-o1-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o1-type"></span></td>
  </c:if>

   			<td><span id="tab-o2-diam"></span></td>
   			<td><span id="tab-o2-offset"></span></td>
   			<td><span id="tab-o2-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o2-type"></span></td>
  </c:if>

   			<td><span id="tab-o3-diam"></span></td>
   			<td><span id="tab-o3-offset"></span></td>
   			<td><span id="tab-o3-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o3-type"></span></td>
  </c:if>

   			<td><span id="tab-o4-diam"></span></td>
   			<td><span id="tab-o4-offset"></span></td>
   			<td><span id="tab-o4-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o4-type"></span></td>
  </c:if>

  <c:if test="${param['type'] == 'GRM'}">
   			<td><span id="tab-o5-diam"></span></td>
   			<td><span id="tab-o5-offset"></span></td>
   			<td><span id="tab-o5-location"></span></td>
  </c:if>	
   		</tr>
   		
   		
  		<tr id="cutpattern2">
   			<td colspan="5"><span id="tab-indent"></span></td>
   			
  <c:if test="${param['type'] == 'WDM'}">
   			<td><span id="tab-o5-diam"></span></td>
   			<td><span id="tab-o5-offset"></span></td>
   			<td><span id="tab-o5-location"></span></td>  			
   			<td><span id="tab-o5-type"></span></td>
  </c:if>

   			<td><span id="tab-o6-diam"></span></td>
   			<td><span id="tab-o6-offset"></span></td>
   			<td><span id="tab-o6-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o6-type"></span></td>
  </c:if>

   			<td><span id="tab-o7-diam"></span></td>
   			<td><span id="tab-o7-offset"></span></td>
   			<td><span id="tab-o7-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o7-type"></span></td>
  </c:if>

   			<td><span id="tab-o8-diam"></span></td>
   			<td><span id="tab-o8-offset"></span></td>
   			<td><span id="tab-o8-location"></span></td>
  <c:if test="${param['type'] == 'WDM'}">   			
   			<td><span id="tab-o8-type"></span></td>
  </c:if>

  <c:if test="${param['type'] == 'GRM'}">
   			<td><span id="tab-o9-diam"></span></td>
   			<td><span id="tab-o9-offset"></span></td>
   			<td><span id="tab-o9-location"></span></td>
   			
   			<td><span id="tab-o10-diam"></span></td>
   			<td><span id="tab-o10-offset"></span></td>
   			<td><span id="tab-o10-location"></span></td>
   			
  </c:if>	

	
   		</tr>
   	</tbody>
</c:if>

<!-- Branch or Threaded -->
   
<c:if test="${param['type'] == 'BR' || param['type'] == 'THM'}">
   
     <thead>
       <tr>
         <th>Tag</th>
         <th>Pcs</th>
         <th>Size</th>            
         <th>Length</th>            
         <th>Type</th>            
         <th>Fitting</th>
    <c:if test="${param['debug']}">
         <th>Span</th>
         <th>Beg Take</th>            
         <th>End Take</th> 
    </c:if>           
       </tr>
     </thead>
     <tbody id="cutbody">
       <tr id="cutpattern">
         <td><span id="tab-tag"></span></td>
         <td><span id="tab-count"></span></td>
         <td><span id="tab-size"></span></td>
         <td><span id="tab-length"></span></td>
         <td><span id="tab-type"></span></td>
         <td><span id="tab-fitting"></span></td>
     <c:if test="${param['debug']}">
         <td><span id="tab-span"></span></td>
         <td><span id="tab-beg-takeout"></span></td>
         <td><span id="tab-end-takeout"></span></td>
     </c:if>
       </tr>
     </tbody>
     
</c:if>
     
   </table>
</body>
</html>