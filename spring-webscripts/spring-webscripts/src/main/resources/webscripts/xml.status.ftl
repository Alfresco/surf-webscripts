<?xml version="1.0" encoding="UTF-8"?>
<#-- MNT-20195: import new utility file. (LM-190130) -->
<#import "error.utils.ftl" as errorLib />

<response>
    <status>
        <code>${status.code}</code>
        <name>${status.codeName}</name>
        <description>${status.codeDescription}</description>
    </status>
    <#--
        MNT-20195: hide stack trace, server and time, show error log number or error message.
        LM-190130: code changes on line 15-20.
    -->
    <#assign errorId = errorLib.getErrorId(status.message)>
    <#if errorId?has_content>
        <errorLogNumber>${errorId}</errorLogNumber>
    <#else>
        <message>${status.message!""}</message>
    </#if>
</response>

<#macro recursestack exception>
    <#if exception.cause?exists>
        <@recursestack exception=exception.cause/>
    </#if>
    <#if exception.cause?exists == false>
        ${exception}
        <#list exception.stackTrace as element>
            ${element}
        </#list>
    <#else>
        ${exception}
        ${exception.stackTrace[0]}
    </#if>
</#macro>
