<%
String validationQuery_attribute_values ="";
String validationQuery_groups ="";
String validationQuery_presentationtype ="";
String validationQuery_result ="";

if (request.getAttribute("validationQuery_attribute_values") != null){
  validationQuery_attribute_values= (String) request.getAttribute("validationQuery_attribute_values");
}

if (request.getAttribute("validationQuery_presentationtype") != null){
 validationQuery_presentationtype = (String) request.getAttribute("validationQuery_presentationtype");
}

if (request.getAttribute("validationQuery_result") != null){
  validationQuery_result = (String) request.getAttribute("validationQuery_result");
}



%>
<table>
<tr>
<td>

<div class="control-group">
  <label class="control-label" for="validationQuery_attribute_values">Attributes and values(attributename: value1 , value2 ...)</label>  
  <div class="controls">
  <textarea class="input-xxlarge" id="validationQuery_attribute_values" Name="validationQuery_attribute_values" rows="5"><%=validationQuery_attribute_values%></textarea>  
  </div>  
</div>
    
  <br>
  <div class="control-group">
  <label class="control-label" for="validationQuery_presentationtype">Presentationtype (download or images etc)</label>
  <div class="controls">
  <input type="text" id="validationQuery_presentationtype" name="validationQuery_presentationtype" class="input-xlarge" value="<%=validationQuery_presentationtype%>">
    </div>  
</div>

<br>
<input class="btn btn-primary" type="button" value="Validate access" onclick="javascript: save('validateQuery');"/>
  </td>
  <td>
  
 <br>
  <div class="control-group">
  <label class="control-label" for="validationQuery_result">Result</label>
  <div class="controls">
  <textarea class="text-info input-xxlarge" id="validationQuery_result" name="validationQuery_result" rows="12" readonly><%=validationQuery_result%></textarea>
    </div>  
</div>

  </td>
  </tr>
</table>