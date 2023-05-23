{
  <#-- Details of the response code -->
  "status" : 
  {
    "code" : ${status.code},
    "name" : "${status.codeName}",
    "description" : "${status.codeDescription}"
  },  
  
  <#-- Exception details -->
  "message" : "${jsonUtils.encodeJSONString(status.message)}",  
  "exception" : "",
  
  <#-- Exception call stack --> 
  "callstack" : 
  [

  ],
  
  <#-- Server details and time stamp -->
  "server" : "",
  "time" : "${date?datetime}"
}

<#macro recursestack exception>
   <#if exception.cause??>
      <@recursestack exception=exception.cause/>
   </#if>
   <#if !exception.cause??>
      ,"${jsonUtils.encodeJSONString(exception?string)}"
      <#list exception.stackTrace as element>
      ,"${jsonUtils.encodeJSONString(element?string)}"
      </#list>
   <#else>
      ,"${jsonUtils.encodeJSONString(exception?string)}"
      ,"${jsonUtils.encodeJSONString(exception.stackTrace[0]?string)}"
   </#if>
</#macro>
