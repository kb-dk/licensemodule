<%@page pageEncoding="UTF-8"%>
<%@ page import="
    java.util.*,
    dk.statsbiblioteket.doms.licensemodule.persistence.*,
    dk.statsbiblioteket.doms.licensemodule.service.*,
    dk.statsbiblioteket.doms.licensemodule.*"%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    <title>Adgangslicens editor, opret/rediger licens</title>
    <script type="text/javascript" src="js/jquery-1.8.3.js"></script>
    <script type="text/javascript" src="js/bootstrap.js"></script>

    <link href="css/bootstrap.min.css" rel="stylesheet" media="screen" />
    <link href="css/licensemodule.css" rel="stylesheet" media="screen" />

</head>
<body>
    <%
     License license = null;
     String licenseId = request.getParameter("licenseId");
     String createNew = request.getParameter("createNew");
     H2Storage storage = H2Storage.getInstance();
     
    if(licenseId != null){ //Edit existing license
      
      
      license = storage.getLicense(Long.parseLong(licenseId));
      session.setAttribute("license",license);
    }           
    else if ("true".equals(createNew)  || session.getAttribute("license") == null){        
      license = new License();                 
      session.setAttribute("license",license);          
    }
    else if (session.getAttribute("license") == null){
        license = new License();
        session.setAttribute("license",license);
    }
    else {
        license = (License) session.getAttribute("license");
    }
%>

<script>
    function addAttributeGroup(){
        document.licenseForm.event.value='addAttributeGroup';
        document.licenseForm.submit();
    }

    function deleteAttributeGroup(attributeGroupNumber){
        document.licenseForm.attributeGroupNumber.value=attributeGroupNumber;
        document.licenseForm.event.value='deleteAttributeGroup';        
        document.licenseForm.submit();
    }

    function addAttribute(attributeGroupNumber){
        document.licenseForm.attributeGroupNumber.value=attributeGroupNumber;
        document.licenseForm.event.value='addAttribute';
        document.licenseForm.submit();
    }

    function addValue(attributeGroupNumber,attributeNumber){
        document.licenseForm.attributeGroupNumber.value=attributeGroupNumber;
        document.licenseForm.attributeNumber.value=attributeNumber;
        document.licenseForm.event.value='addValue';
        document.licenseForm.submit();
    }


    function save(){
        document.licenseForm.event.value='save';
        document.licenseForm.submit();
    }

    function confirmDelete(message) {
       var answer = confirm(message);
       if (answer){
          deleteLicense();       
       }
     }
      
     function deleteLicense(){      
       document.licenseForm.event.value='delete';
       document.licenseForm.submit();
    }        
</script>

        
 
<h1>Adgangslicens editor, opret/rediger licens</h1>

<%@ include file="message.jsp" %>

<ul class="nav nav-tabs" id="licenseTab">
    <li class="active"><a href="#access">Hvem skal have adgang</a></li>
    <li><a href="#access_who">Hvad skal der gives adgang til</a></li>
</ul>

<form name="licenseForm" class="well" action="createLicenseServlet" method="POST">
    <input type="hidden" name="event" />
    <input type="hidden" name="attributeGroupNumber" />
    <input type="hidden" name="attributeNumber" />

    <div class="tab-content">
        <div class="tab-pane active" id="access">
            <%@ include file="access.jsp" %>
        </div>
        <div class="tab-pane" id="access_who">
            <%@ include file="access_who.jsp" %>
        </div>
    </div>
</form>

<script>
    $('#licenseTab a').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    })
</script>

</body>
</html>