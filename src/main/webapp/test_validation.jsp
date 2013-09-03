<%
String validation_attribute_values ="";
String validation_groups ="";
String validation_presentationtype ="";
String validation_result ="";

if (request.getAttribute("validation_attribute_values") != null){
  validation_attribute_values= (String) request.getAttribute("validation_attribute_values");
}

if (request.getAttribute("validation_groups") != null){
   validation_groups= (String) request.getAttribute("validation_groups");
}

if (request.getAttribute("validation_presentationtype") != null){
 validation_presentationtype = (String) request.getAttribute("validation_presentationtype");
}

if (request.getAttribute("validation_result") != null){
  validation_result = (String) request.getAttribute("validation_result");
}

%>
<table>
<tr>
<td>

<div class="control-group">
  <label class="control-label" for="validation_attribute_values">Attributes and values(attributename: value1 , value2 ...)</label>  
  <div class="controls">
  <textarea class="input-xxlarge" id="validation_attribute_values" Name="validation_attribute_values" rows="5"><%=validation_attribute_values%></textarea>  
  </div>  
</div>

  <br>
  <div class="control-group">
  <label class="control-label" for="validation_groups">Licens grupper (licensgruppe1 , licensgruppe2  , ...)</label>
  <div class="controls">
  <input type="text" id="validation_groups" name="validation_groups" class="input-xxlarge" value="<%=validation_groups%>">
    </div>  
</div>
  
  
  <br>
  <div class="control-group">
  <label class="control-label" for="validation_presentationtype">Presentationtype (download , images , ...)</label>
  <div class="controls">
  <input type="text" id="validation_presentationtype" name="validation_presentationtype" class="input-xlarge" value="<%=validation_presentationtype%>">
    </div>  
</div>

<br>
<input class="btn btn-primary" type="button" value="Validate access" onclick="javascript: save('validate');"/>
  </td>
  <td>
  
 <br>
  <div class="control-group">
  <label class="control-label" for="validation_result">Result</label>
  <div class="controls">
  <textarea class="text-info input-xxlarge" id="validation_result" name="validation_result" rows="16" readonly><%=validation_result%></textarea>
    </div>  
</div>

  </td>
  </tr>
</table>