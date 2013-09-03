
<h1>API for Licensemodule REST resources. </h1>
<h2>Version:${pom.version}</h2>
<h2>Build time:${build.time}</h2>
<br>                                                       
<h2> SERVICE METHODS: </h2>

<strong>Supported locales: da , en</strong>
<br>


<table class="table" border="1">  
       <caption><strong>HTTP POST, application type:  text/xml</strong></caption>
        <thead>  
          <tr>  
            <th>URL</th>  
            <th>Input XML example</th>                
            <th>Output XML example</th>
          </tr>  
        </thead>  
        <tbody>            
            <tr>  
            <td>/services/getUserLicenseQuery</td>  
            <td>
            &lt;getUserQueryInputDTO&gt;<br>
            &lt;attributes&gt;<br>
            &lt;attribute&gt;attribut_store.MediestreamFullAccess&lt;/attribute&gt;<br>
            &lt;values&gt;true&lt;/values&gt;<br>
            &lt;/attributes&gt;<br>
            &lt;presentationType&gt;images&lt;/presentationType&gt;<br>
            &lt;/getUserQueryInputDTO&gt;<br>
            </td>  
            <td>(((group:"reklamefilm") OR (group:"individuelt")) -(group:"klausuleret"))</td>
          </tr>                             
          <tr>  
            <td>/services/checkAccessForIds</td>  
            <td>
             &lt;checkAccessForIdsInputDTO&gt;<br>
             &lt;attributes&gt;<br>
             &lt;attribute&gt;attribut_store.MediestreamFullAccess&lt;/attribute&gt;<br>
             &lt;values&gt;true&lt;/values&gt;<br>
             &lt;/attributes&gt;<br>
             &lt;ids&gt;doms_radioTVCollection:uuid:a5390b1e-69fb-47c7-b23e-7831eb59479d&lt;/ids&gt;<br>
             &lt;ids&gt;doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec&lt;/ids&gt;<br>
             &lt;presentationType&gt;images&lt;/presentationType&gt;<br>
             &lt;/checkAccessForIdsInputDTO&gt;<br>
            </td>  
            <td>
            &lt;checkAccessForIdsOutputDTO&gt;<br>
            &lt;accessIds&gt;doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec&lt;/accessIds&gt;<br>
            &lt;presentationType&gt;images&lt;/presentationType&gt;<br>
            &lt;query&gt;(((group:&quot;reklamefilm&quot;) OR (group:&quot;individuelt&quot;)) -(group:&quot;klausuleret&quot;))&lt;/query&gt;<br>
            &lt;/checkAccessForIdsOutputDTO&gt;<br>
            </td>
          </tr>           
          <tr>  
            <td>/services/getUserLicenses</td>  
            <td>
           &lt;getUsersLicensesInputDTO&gt;<br>
           &lt;attributes&gt;<br>
           &lt;attribute&gt;attribut_store.MediestreamFullAccess&lt;/attribute&gt;<br>
           &lt;values&gt;true&lt;/values&gt;<br>
           &lt;/attributes&gt;<br>
           &lt;locale&gt;da&lt;/locale&gt;<br>
           &lt;/getUsersLicensesInputDTO&gt;<br>
             </td>               
             <td>
             &lt;getUsersLicensesOutputDTO&gt;<br>
             &lt;licenses&gt;<br>
             &lt;description&gt;beskrivelse mediastream&lt;/description&gt;<br>
             &lt;name&gt;mediastream&lt;/name&gt;<br>
             &lt;validFrom&gt;01-01-2001&lt;/validFrom&gt;<br>
             &lt;validTo&gt;01-01-2030&lt;/validTo&gt;<br>
             &lt;/licenses&gt;<br>
             &lt;/getUsersLicensesOutputDTO&gt;<br>             
             </td>  
          </tr>  
          <tr>  
            <td>/services/validateAccess</td>  
            <td>
              &lt;validateAccessInputDTO&gt;<br>
              &lt;attributes&gt;<br>
              &lt;attribute&gt;wayf.schacHomeOrganization&lt;/attribute&gt;<br>
              &lt;values&gt;sb.dk&lt;/values&gt;<br>
              &lt;values&gt;test.dk&lt;/values&gt;<br>
              &lt;/attributes&gt;<br>
              &lt;groups&gt;Individuelt forbud&lt;/groups&gt;<br>
              &lt;groups&gt;Klausuleret&lt;/groups&gt;<br>
              &lt;presentationType&gt;images&lt;/presentationType&gt;<br>
              &lt;/validateAccessInputDTO&gt;<br>
            </td>  
            <td>
            &lt;validateAccessOutputDTO&gt;;<br>
            &lt;access&gt;false&lt;/access&gt;;<br>
            &lt;/validateAccessOutputDTO&gt;;<br>
            </td>
          </tr>         
          <tr>  
            <td>/services/getUserGroups</td>  
            <td>
            &lt;getUserGroupsInputDTO&gt;<br>
            &lt;attributes&gt;<br>
            &lt;attribute&gt;attribut_store.MediestreamFullAccess&lt;/attribute&gt;<br>
            &lt;values&gt;true&lt;/values&gt;<br>
            &lt;/attributes&gt;<br>
            &lt;locale&gt;da&lt;/locale&gt;<br>
            &lt;/getUserGroupsInputDTO&gt;<br>
            </td>  
            <td>
            &lt;getUserGroupsOutputDTO&gt;<br>
            &lt;groups&gt;<br>
            &lt;groupName&gt;DR 1 TV&lt;/groupName&gt;<br>
            &lt;presentationTypes&gt;search&lt;/presentationTypes&gt;<br>
            &lt;/groups&gt;<br>
            &lt;groups&gt;<br>
            &lt;groupName&gt;DR Radio&lt;/groupName&gt;<br>
            &lt;presentationTypes&gt;search&lt;/presentationTypes&gt;<br>
            &lt;presentationTypes&gt;download&lt;/presentationTypes&gt;<br>
            &lt;/groups&gt;<br>
            &lt;groups&gt;<br>
            &lt;groupName&gt;Individuelt forbud&lt;/groupName&gt;<br>
            &lt;presentationTypes&gt;full stream&lt;/presentationTypes&gt;<br>
            &lt;presentationTypes&gt;images&lt;/presentationTypes&gt;<br>
            &lt;/groups&gt;V
            &lt;groups&gt;<br>
            &lt;groupName&gt;Reklamefilm&lt;/groupName&gt;<br>
            &lt;presentationTypes&gt;search&lt;/presentationTypes&gt;<br>
            &lt;presentationTypes&gt;images&lt;/presentationTypes&gt;<br>
            &lt;presentationTypes&gt;10 sec. stream&lt;/presentationTypes&gt;<br>
            &lt;presentationTypes&gt;download&lt;/presentationTypes&gt;<br>
            &lt;/groups&gt;<br>
            &lt;groups&gt;<br>
            &lt;groupName&gt;TV2 TV&lt;/groupName&gt;<br>
            &lt;presentationTypes&gt;search&lt;/presentationTypes&gt;<br>
            &lt;/groups&gt;<br>
            &lt;/getUserGroupsOutputDTO&gt;<br>            
            </td>
          </tr>         
        </tbody>  
</table>    
<br>

<table class="table" border="1">  
       <caption><strong>HTTP POST, application type:  application/json</strong></caption>
        <thead>  
          <tr>  
            <th>URL</th>  
            <th>Input JSON example</th>                
            <th>Output JSON examle</th>
          </tr>  
        </thead>  
        <tbody>                                                 
          <tr>  
            <td>/services/checkAccessForIds</td>  
            <td>
             {"attributes":[{"attribute":"attribut_store.MediestreamFullAccess","values":["true","yes"]}],"presentationType":"images","ids":["doms_radioTVCollection:uuid:a5390b1e-69fb-47c7-b23e-7831eb59479d","doms_reklamefilm:uuid:35a1aa76-97a1-4f1b-b5aa-ad2a246eeeec"]}
            </td>  
            <td>
            {"query":"(((group:\"tv2tv\") OR (group:\"klausuleret\") OR (group:\"dr1tv\") OR (group:\"individuelt\")))","presentationType":"images","accessIds":["doms_radioTVCollection:uuid:a5390b1e-69fb-47c7-b23e-7831eb59479d"]}
            </td>
          </tr>           
          <tr>  
            <td>/services/getUserLicenses</td>  
            <td>
             {"attributes":[{"attribute":"attribut_store.MediestreamFullAccess","values":["true","yes"]}],"locale":"da"}
             </td>               
             <td>
               {"licenses":[{"name":"Dighumlab adgang","description":"info of hvem licensen vedr. og hvad der er adgang til","validFrom":"27-12-2012","validTo":"27-12-2023"}]}
             </td>  
          </tr>          
          <tr>  
            <td>/services/getUserGroups</td>  
            <td>
           {"attributes":[{"attribute":"attribut_store.MediestreamFullAccess","values":["true","yes"]}],"locale":"da"}
            </td>  
            <td>
           {"groups":[{"presentationTypes":["search","images"],"groupName":"DR1"},{"presentationTypes":["images"],"groupName":"IndividueltForbud"},{"presentationTypes":["images"],"groupName":"Klausuleret"},{"presentationTypes":["search"],"groupName":"Reklamefilm"},
             {"presentationTypes":["search","images","download"],"groupName":"TV2"}]}       
            </td>
          </tr>         
             <tr>  
            <td>/services/getUserGroupsAndLicenses</td>  
            <td>
           {"attributes":[{"attribute":"attribut_store.MediestreamFullAccess","values":["true","yes"]}],"locale":"en"}          
            </td>  
            <td>
            {"groups":[{"presentationTypes":["Search"],"groupName":"Commercial collection"},</br>
                       {"presentationTypes":["Search"],"groupName":"Radio collection"},</br>
                       {"presentationTypes":["Search"],"groupName":"TV collection"}],</br>
                       "licenses":[{"name":"Anonymous","description":"Rights to full search of the data but no watching or listening","validFrom":"01-01-2013","validTo":"01-01-2014"}],</br>
                       "allPresentationTypes":["Search","Stream","Download","View thumbnails"],</br>
                       "allGroups":["Commercial collection","Held in trust","Individual ban","Radio collection","TV collection"]}       
            </td>
          </tr>
        </tbody>  
</table>    
<br>



<table class="table" border="1">  
       <caption><strong>HTTP errors</strong></caption>
        <thead>  
          <tr>  
            <th>Error</th>  
            <th>Reason</th>                
          </tr>  
        </thead>  
        <tbody>  
          <tr>  
            <td>400 (Bad Request)</td>  
            <td>Caused by the input. Validation error etc.</td>    
          </tr>  
            <tr>  
            <td>500 (Internal Server Error)</td>  
            <td>Server side errors, nothing to do about it.</td>    
          </tr>
        </tbody>  
</table>    
