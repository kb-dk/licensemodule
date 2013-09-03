
<%
    String selectName = (String) request.getAttribute("selectName");
    String selectValue = (String) request.getAttribute("selectValue");
    ArrayList<ConfiguredDomAttributeType> options= LicenseCache.getConfiguredDomAttributeTypes();
%>

<select name="<%=selectName%>" class="span4">  
 <option></option>
 <%
   for (int option=0;option<options.size();option++){
   String current = options.get(option).getValue();
   if (current.equals(selectValue)){%>
      <option selected><%=current%></option>  
   <%}
   else{ %>
   <option><%=current%></option>
   <%}
  }%>
      
</select>
  