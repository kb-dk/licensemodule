<% int zebra = 0;  
%>

<strong>Server start:<%=MonitorCache.SERVER_START_TIME%></strong></br>


<table class="table table-condensed table-hover">
   <caption><strong>Rest method calls since server restart </strong> </caption>
   <thead>
   <tr>
    <th>Method name</th>
    <th>#Calls</th>
   </tr>
   </thead>
   <tbody>
  <% for (String current :MonitorCache.REST_METHOD_CALLS.keySet()){
  %>
  
   <tr class="<%=Util.getStyle(zebra++)%>">
      <td><%=current%></td>
      <td><%=MonitorCache.REST_METHOD_CALLS.get(current)%></td>
  </tr>
<%}%>

   </tbody>
</table>