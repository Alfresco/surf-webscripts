{
  <#-- Details of the response code -->
  "status" : 
  {
    "code" : ${status.code},
    "name" : "${status.codeName}",
    "description" : "${status.codeDescription}"
  },  
  
  <#--
  	MNT-20195: hide Exception details, call stack, Server details and timestamp. Show either error log number or error message.
  	LM-190130: code changes on line 28-36.
  -->
  <#import "error.utils.ftl" as errorLib />
  <#assign errorId = errorLib.getErrorId(status.message)>
  <#if errorId?has_content>
  <#-- Error Log Number -->
  "errorLogNumber": "${errorId}"
  <#else>
  <#-- Exception message -->
  "message" : "${jsonUtils.encodeJSONString(status.message)}"
  </#if>
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