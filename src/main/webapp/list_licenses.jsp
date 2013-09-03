<%  
   ArrayList<License> licenses = storage.getAllLicenseNames();
%>
  <table class="table table-condensed table-hover">
   <thead>
   <tr>
    <th>ID</th>
    <th>Name</th>
    <th>Valid from</th>
   <th>Valid to</th>
   </tr>
   </thead>
   <tbody>
<% for (int i = 0;i< licenses.size();i++ ){
  License current = licenses.get(i);
%>
   <tr class="<%=Util.getStyle(i)%>" onclick="window.location = 'license.jsp?licenseId=<%=current.getId()%>';">
      <td><%=current.getId()%></td>
      <td><%=current.getLicenseName()%><br><%=current.getLicenseName_en()%></td>
      <td><%=current.getValidFrom()%></td>
      <td><%=current.getValidTo()%></td>
  </tr>
<%}%>
   </tbody>
</table>
  
 <a class="btn btn-primary" href="license.jsp?createNew=true">Opret ny licens</a>
  
  