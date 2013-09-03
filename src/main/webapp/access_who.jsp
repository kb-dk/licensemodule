
<%
 ArrayList<ConfiguredDomLicenseGroupType> configuredDomGroups = LicenseCache.getConfiguredDomLicenseGroupTypes();
 ArrayList<ConfiguredDomLicensePresentationType> configuredDomsLicenseTypes = LicenseCache.getConfiguredDomLicenseTypes();

 //variable 'license' is already known here, from the page that included this page
 ArrayList<LicenseContent> domGroups =  license.getLicenseContents();

%>

<table class="table table-condensed table-hover">
   <thead>
   <tr>
    <th class="domsGroups">Doms grupper</th>
    <th class="allowedGroups">Tiladte pr&aelig;sentationsgrupper</th>
   </tr>
   </thead>
   <tbody>
   <%
for (int i=0;i<configuredDomGroups.size();i++){
  String currentGroupKey = configuredDomGroups.get(i).getKey();
  String currentGroupName = configuredDomGroups.get(i).getValue_dk();
%>
<tr class="<%=Util.getStyle(i)%>">
  <td  class="domsGroups">
    <label class="checkbox">
      <input type="checkbox" <%if(Util.domGroupsContainGroupName(domGroups, currentGroupKey)){out.println("checked");}%> name="domsGruppe_<%=i%>"> <%=currentGroupName%> <%if (configuredDomGroups.get(i).isMustGroup()){out.println(" (MUST) ");} %>
    </label>
  </td>
  <td class="allowedGroups">
   <%
   for (int j=0;j<configuredDomsLicenseTypes.size();j++){
   String presentationTypeKey=configuredDomsLicenseTypes.get(j).getKey();
    String presentationTypeName=configuredDomsLicenseTypes.get(j).getValue_dk();
   %>
  <label class="checkbox">
      <input type="checkbox"  <%if(Util.domGroupsContainsGroupWithLicense(domGroups, currentGroupKey,presentationTypeKey)){out.println("checked");}%> name="domsGruppe_<%=i%>_license_<%=j%>"> <%=presentationTypeName%>
    </label>
   <%}%>
 </td>
</tr>
<%}%>
</tbody>
</table>

<input class="btn btn-primary" type="button" value="Gem" onclick="javascript: save();"/>
<a class="btn btn-small" href="configuration.jsp">Fortryd</a>
<% if (license.getId() != 0){%>
  <input class="btn btn-primary btn-delete" type="button" value="Slet" onclick="javascript: confirmDelete('Delete license:<%=license.getLicenseName()%>');"/>
<%}%>