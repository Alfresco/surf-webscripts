<style>
    <!--
    #app5607893292_appbody { padding-left: 7px; }
    -->
</style>

<div id="appbody">

    <!-- MNT-20195: import error utility file. (LM-190130) -->
    <#import "error.utils.ftl" as errorLib />

    <table>
        <tr>
            <td><img src="${url.context}/images/logo/AlfrescoLogo32.png" alt="Alfresco" /></td>
            <td><nobr><span class="title">Web Script Status ${status.code} - ${status.codeName}</span></nobr></td>
        </tr>
    </table>
    <br>
    <table>
        <tr><td><b>Error:</b><td>${status.codeName} (${status.code}) - ${status.codeDescription}
        <tr><td>&nbsp;
                <!--
                    MNT-20195: hide server, time and stacktrace info, show error log id or message.
                    LM-190310: code changes on line 26-31.
                -->
                <#assign errorId = errorLib.getErrorId(status.message)>
                <#if errorId?has_content>
        <tr><td><b>Error Log Number:</b><td>${errorId}
                <#else>
        <tr><td><b>Message:</b><td>${status.message!"<i>&lt;Not specified&gt;</i>"}
                </#if>
        <tr><td>&nbsp;
    </table>

</div>


<#macro recursestack exception>
    <#if exception.cause?exists>
        <@recursestack exception=exception.cause/>
    </#if>
    <tr><td><b>Exception:</b><td>${exception.class.name}<#if exception.message?exists> - ${exception.message}</#if>
    <tr><td><td>&nbsp;
    <#if exception.cause?exists == false>
        <#list exception.stackTrace as element>
            <tr><td><td>${element}
        </#list>
    <#else>
        <tr><td><td>${exception.stackTrace[0]}
    </#if>
    <tr><td><td>&nbsp;
</#macro>