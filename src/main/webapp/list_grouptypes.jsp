<%  
  ArrayList<ConfiguredDomLicenseGroupType> configuredGroupTypes = LicenseCache.getConfiguredDomLicenseGroupTypes();
%>
<table class="table table-condensed table-hover">
   <thead>
   <tr>
    <th class="id">ID</th>
    <th class="mustGroup">Must group</th>
    <th>Key</th>
    <th>Value</th>    
    <th>Description</th>
    <th>Query string</th>
    <th></th>
   </tr>
   </thead>
   <tbody>
<% for (int i = 0;i< configuredGroupTypes.size();i++ ){
  ConfiguredDomLicenseGroupType current = configuredGroupTypes.get(i);
%>
   <tr class="<%=Util.getStyle(i)%>">
      <td class="id"><%=current.getId()%></td>
      <td class="mustGroup"><input disabled type="checkbox" <%if(current.isMustGroup()){out.println("checked");}%> ></td>
      <td><%=current.getKey()%></td>
      <td><%=current.getValue_dk()%><br><%=current.getValue_en()%></td>      
      <td><%=current.getDescription_dk()%><br><%=current.getDescription_en()%></td>
      <td><%=current.getQuery()%></td>      
      <td>
        <a class="btn btn-primary" href="edit_grouptype.jsp?grouptypeId=<%=current.getId()%>">Rediger</a>
        <input class="btn btn-primary btn-delete" type="button" value="Slet" onclick="javascript: confirmDeleteGroupType('Delete group type:<%=current.getKey()%>','<%=current.getKey()%>');"/>
      </td>
  </tr>

<%}%>
   </tbody>
</table>

<div class="infoGroup">
  <span class="help-inline">Key</span>  
  <input type="text" name="key_grouptype" class="span3" value="">
  <span class="help-inline">V&aelig;rdi</span>  
  <input type="text" name="value_grouptype" class="span3" value="">
  <span class="help-inline">Beskrivelse</span>  
  <input type="text" name="value_groupdescription" class="span3" value="">
  <span class="help-inline">Query</span>
  <input type="text" name="value_groupquery" class="span3" value="">
  <input type="checkbox" name="mustGroupCheck"> Must group
</div>
<br>
<div class="infoGroup">
  <span class="help-inline">V&aelig;rdi(En)</span>  
  <input type="text" name="value_en_grouptype" class="span3" value="">
  <span class="help-inline">Beskrivelse(En)</span>  
  <input type="text" name="value_en_groupdescription" class="span3" value="">
  <input class="btn btn-primary" type="button" value="Opret ny gruppetype" onclick="javascript: save('save_grouptype');"/>
 </div>