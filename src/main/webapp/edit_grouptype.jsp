<%@page pageEncoding="UTF-8"%>
<%@ page import="
    java.util.*,
    dk.statsbiblioteket.doms.licensemodule.persistence.*,
    dk.statsbiblioteket.doms.licensemodule.service.*,
    org.apache.commons.lang.StringEscapeUtils,
    dk.statsbiblioteket.doms.licensemodule.*"%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <title>Rediger license gruppe</title>
    <script language="javascript" type="text/javascript" src="js/jquery-1.8.3.js"></script>
    <script language="javascript" type="text/javascript" src="js/bootstrap.js"></script>

    <link href="css/bootstrap.min.css" rel="stylesheet" media="screen" />
    <link href="css/licensemodule.css" rel="stylesheet" media="screen" />

</head>
<body>
<%
String id = request.getParameter("grouptypeId");
ArrayList<ConfiguredDomLicenseGroupType> allTypes  = LicenseCache.getConfiguredDomLicenseGroupTypes();      
ConfiguredDomLicenseGroupType edit = null;
for ( ConfiguredDomLicenseGroupType current : allTypes){
   if (( current.getId() + "").equals(id)){
    edit=current;
    break;
   }
}

%>

<script>
    function updateGroup(id){
        document.configurationForm.event.value='updateGroup';
        document.configurationForm.id.value=id;
        document.configurationForm.submit();
    }

</script>

<h1>Adgangslicens editor, rediger license gruppe </h1>
<br>
<form name="configurationForm" class="well" action="configurationServlet" method="POST">
    <input type="hidden" name="event" />
    <input type="hidden" name="id" />

<div class="infoGroup">  
  <span class="help-inline">Key</span>  
  <input type="text" name="key_grouptype" class="span3" readonly="true" value="<%=edit.getKey()%>">
  <span class="help-inline">V&aelig;rdi</span>  
  <input type="text" name="value_grouptype" class="span3" value="<%=edit.getValue_dk()%>">
  <span class="help-inline">Beskrivelse</span>  
  <input type="text" name="value_groupdescription" class="span3" value="<%=edit.getDescription_dk()%>">
  <span class="help-inline">Query</span>
  <input type="text" name="value_groupquery" class="span3" value="<%=StringEscapeUtils.escapeHtml(edit.getQuery())%>">
  <input type="checkbox"   name="mustGroupCheck"  <%if(edit.isMustGroup()){out.println("checked");}%> > Must group
</div>
<div class="infoGroup">
  <span class="help-inline">V&aelig;rdi(En)</span>  
  <input type="text" name="value_en_grouptype" class="span3" value="<%=edit.getValue_en()%>">
  <span class="help-inline">Beskrivelse(En)</span>  
  <input type="text" name="value_en_groupdescription" class="span3" value="<%=edit.getDescription_en()%>">   
  <input class="btn btn-primary" type="button" value="Opdater" onclick="javascript: updateGroup('<%=edit.getId()%>');"/>
  <a class="btn btn-small" href="configuration.jsp">Fortryd</a>
</div>
</form>

</body>

</html>