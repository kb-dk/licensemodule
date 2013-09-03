<%  
  ArrayList<ConfiguredDomAttributeType> configuredAttributeTypes = LicenseCache.getConfiguredDomAttributeTypes();
%>
<table class="table table-condensed table-hover">
   <thead>
   <tr>
    <th>ID</th>
    <th>Value</th>
    <th></th>
   </tr>
   </thead>
   <tbody>
<% for (int i = 0;i< configuredAttributeTypes.size();i++ ){
  ConfiguredDomAttributeType current = configuredAttributeTypes.get(i);
%>
   <tr class="<%=Util.getStyle(i)%>">
      <td><%=current.getId()%></td>
      <td><%=current.getValue()%></td>
      <td><input class="btn btn-primary btn-delete" type="button" value="Slet" onclick="javascript: confirmDeleteAttributeType('Delete atribute type:<%=current.getValue()%>','<%=current.getValue()%>');"/></td>
  </tr>
<%}%>
   </tbody>
</table>

<input type="text" name="value_attributetype" class="span3" value="">
<input class="btn btn-primary" type="button" value="Opret ny attributtype" onclick="javascript: save('save_attributetype');"/>
  