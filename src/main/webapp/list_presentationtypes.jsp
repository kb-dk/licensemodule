<%
  ArrayList<ConfiguredDomLicensePresentationType> configuredLicenseTypes = LicenseCache.getConfiguredDomLicenseTypes();    
%>
<table class="table table-condensed table-hover">
   <thead>
   <tr>
    <th>ID</th>
    <th>Key</th>
    <th>Value</th>    
    <th></th>
   </tr>
   </thead>
   <tbody>
<% for (int i = 0;i< configuredLicenseTypes.size();i++ ){
  ConfiguredDomLicensePresentationType current = configuredLicenseTypes.get(i);
%>
   <tr class="<%=Util.getStyle(i)%>">
      <td><%=current.getId()%></td>
      <td><%=current.getKey()%></td>
      <td><%=current.getValue_dk()%><br><%=current.getValue_en()%></td>              
      <td>
        <a class="btn btn-primary" href="edit_presentationtype.jsp?presentationtypeId=<%=current.getId()%>">Rediger</a>
        <input class="btn btn-primary btn-delete" type="button" value="Slet" onclick="javascript: confirmDeletePresentationType('Delete presentationtype:<%=current.getKey()%>','<%=current.getKey()%>');"/>
      </td>      
  </tr>

<%}%>
   </tbody>
</table>

<div class="newPresentationGroup">
  <span class="help-inline">Key</span>
  <input type="text" name="key_presentationtype" class="span3" value="">
  <span class="help-inline">V&aelig;rdi</span>
  <input type="text" name="value_presentationtype" class="span3" value="">
  <span class="help-inline">V&aelig;rdi(En)</span>
  <input type="text" name="value_en_presentationtype" class="span3" value="">
  <input class="btn btn-primary" type="button" value="Opret ny pr&aelig;sentationstype" onclick="javascript: save('save_presentationtype');"/>
</div>
  