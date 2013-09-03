 
<%
// variable 'license' is allready known on this place
 ArrayList<AttributeGroup> groups= license.getAttributeGroups();
%>
<div class="infoGroup">
    <div class="inputGroup">
        <span class="help-inline">Licensnavn</span>
        <input type="text" name="licenseName" class="span3" value="<%=license.getLicenseName()%>">
    </div>
    <div class="inputGroup">
        <span class="help-inline">Beskrivelse</span>
        <input type="text" name="description"  class="span3" value="<%=license.getDescription_dk()%>">
    </div>
    <div class="inputGroup from">
        <span class="help-inline">Licens gyldig fra</span>
        <input type="text"  name="validFrom" class="span2" value="<%=license.getValidFrom()%>">
    </div>
    <div class="inputGroup">
        <span class="help-inline">og til</span>
        <input type="text"  name="validTo" class="span2" value="<%=license.getValidTo()%>">
    </div>
</div>
<div class="infoGroup">
    <div class="inputGroup">
        <span class="help-inline">Licensnavn(En)</span>
        <input type="text" name="licenseName_en" class="span3" value="<%=license.getLicenseName_en()%>">
    </div>
    <div class="inputGroup">
        <span class="help-inline">Beskrivelse(En)</span>
        <input type="text" name="description_en"  class="span3" value="<%=license.getDescription_en()%>">
    </div>
</div>


  <table class="table table-condensed table-hover">
    <thead>
   <tr>
    <th class="attributeGroup">Attributgruppe</th>
    <th class="attribute">Attribut</th>
    <th class="attributeValue">V&aelig;rdi</th>
   </tr>
   </thead>
   <tbody>
<%
for (int i=0;i<groups.size();i++){
AttributeGroup currentGroup = groups.get(i);
%>
 <%@ include file="blocks/attributegroup.jsp" %>

<%
}
%>
   <tr>
    <td><input class="btn" type="button" value="Ny" onclick="javascript: addAttributeGroup();"/></td>
    <td></td>
    <td></td>
   </tr>
    
  </tbody>
  </table>

    <input class="btn btn-primary" type="button" value="Next" onclick="javascript: $('#licenseTab li:eq(1) a').tab('show');"/>
    <a class="btn btn-small" href="configuration.jsp">Fortryd</a>
    

