<%
String checkAccessIds_attribute_values ="";
String checkAccessIds_presentationtype ="";
String checkAccessIds_ids ="";
String checkAccessIds_result ="";

if (request.getAttribute("checkAccessIds_attribute_values") != null){
  checkAccessIds_attribute_values= (String) request.getAttribute("checkAccessIds_attribute_values");
}

if (request.getAttribute("checkAccessIds_presentationtype") != null){
 checkAccessIds_presentationtype = (String) request.getAttribute("checkAccessIds_presentationtype");
}

if (request.getAttribute("checkAccessIds_ids") != null){
  checkAccessIds_ids = (String) request.getAttribute("checkAccessIds_ids");
}

if (request.getAttribute("checkAccessIds_result") != null){
  checkAccessIds_result = (String) request.getAttribute("checkAccessIds_result");
}



%>
<table>
<tr>
<td>

<div class="control-group">
  <label class="control-label" for="checkAccessIds_attribute_values">Attributes and values(attributename: value1 , value2 ...)</label>  
  <div class="controls">
  <textarea class="input-xxlarge" id="checkAccessIds_attribute_values" Name="checkAccessIds_attribute_values" rows="5"><%=checkAccessIds_attribute_values%></textarea>  
  </div>  
</div>
    
<br>
<div class="control-group">
  <label class="control-label" for="checkAccessIds_presentationtype">Presentationtype (download or images etc)</label>
  <div class="controls">
  <input type="text" id="checkAccessIds_presentationtype" name="checkAccessIds_presentationtype" class="input-xlarge" value="<%=checkAccessIds_presentationtype%>">
    </div>  
</div>

<br>
<div class="control-group">
  <label class="control-label" for="checkAccessIds_ids">RecordIds (id1, id2 , ...)</label>
  <div class="controls">
  <input type="text" id="checkAccessIds_ids" name="checkAccessIds_ids" class="input-xlarge" value="<%=checkAccessIds_ids%>">
    </div>  
</div>


<br>
<input class="btn btn-primary" type="button" value="Validate Ids" onclick="javascript: save('checkAccessIds');"/>
  </td>
  <td>
  
 <br>
  <div class="control-group">
  <label class="control-label" for="checkAccessIds_result">Result</label>
  <div class="controls">
  <textarea class="text-info input-xxlarge" id="checkAccessIds_result" name="checkAccessIds_result" rows="16" readonly><%=checkAccessIds_result%></textarea>
    </div>  
</div>

  </td>
  </tr>
</table>