  <%
  String showNumber="";
  ArrayList<Attribute> attributes = currentGroup.getAttributes();
    
  for (int j=0;j<attributes.size();j++) {
  Attribute attribute = attributes.get(j);
  ArrayList<AttributeValue> values = attribute.getValues();
  %>
  
  <tr class="<%=Util.getStyle(currentGroup.getNumber())%>" >
    <td class="attributeGroup">
    <%if (j==0){%>    
      <%=currentGroup.getNumber()%> 
      <input class="btn btn-primary btn-delete" type="button" value="Slet" onclick="javascript:deleteAttributeGroup(<%=currentGroup.getNumber()%>);"/>
    <%}%></td>
    <td class="attribute">
    <%    
    request.setAttribute("selectName","attributegroup_"+currentGroup.getNumber()+"_attribute"+j);
    request.setAttribute("selectValue",attribute.getAttributeName());    
    %>    
     <%@ include file="attribute_select.jsp"%>
    </td>
    <td class="attributeValue">
    <input type="text" name="attributegroup_<%=currentGroup.getNumber()%>_attribute<%=j%>_value0" class="span2" value="<%=values.get(0).getValue()%>">
     <% if (1 == (values.size())) {%>
      <input class="btn" type="button" value="Ny" onclick="javascript:addValue(<%=currentGroup.getNumber()%>,<%=j%>);"/>  
   <%}%>
    
    </td>
  </tr>
  <%
    for (int k = 1;k<values.size();k++){%>
  <tr class="<%=Util.getStyle(currentGroup.getNumber())%>" >
    <td class="attributeGroup"></td>
    <td class="attribute"></td>
    <td class="attributeValue">
    <input type="text" name="attributegroup_<%=currentGroup.getNumber()%>_attribute<%=j%>_value<%=k%>" class="span2" value="<%=values.get(k).getValue()%>">
   <% if (k == (values.size()-1)) {%>
    <input class="btn" type="button" value="Ny" onclick="javascript:addValue(<%=currentGroup.getNumber()%>,<%=j%>);"/>  
   <%}%>
   </td>
 
  </tr>
              
  <%}%>
 <%}%>
    <tr class="<%=Util.getStyle(currentGroup.getNumber())%>">
    <td class="attributeGroup"></td>
    <td class="attribute"><input class="btn" type="button" value="Ny" onclick="javascript:addAttribute(<%=currentGroup.getNumber()%>);"/></td>
    <td class="attributeValue"></td>
   </tr>    
