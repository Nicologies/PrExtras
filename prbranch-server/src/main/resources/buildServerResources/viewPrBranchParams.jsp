<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="settings" class="com.nicologies.prbranch.SettingsBean"/>

<c:choose>
  <c:when test="${propertiesBean.properties[settings.authType] == settings.tokenAuthType}">
    <div class="parameter">
      Github Token: <strong><props:displayValue name="${settings.githubToken}" emptyValue="not specified"/></strong>
    </div>
  </c:when>
  <c:when test="${propertiesBean.properties[settings.authType] == settings.usernameAuthType">
    <div class="parameter">
      Github Username: <strong><props:displayValue name="${settings.githubUsername}" emptyValue="not specified"/></strong>
    </div>
  </c:when>
  <c:when test="${propertiesBean.properties[settings.authType] == settings.systemWideTokenAuthType">
    <div class="parameter">
       <strong>System Wide Github Token</strong>
    </div>
  </c:when>
</c:choose>

<div class="parameter">
  Export branch name as configuration parameter: <strong><props:displayValue name="${settings.exportParamName}" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
  Append branch name to build num? <strong><props:displayCheckboxValue name="${settings.AppendToBuildNum}"/></strong>
</div>
