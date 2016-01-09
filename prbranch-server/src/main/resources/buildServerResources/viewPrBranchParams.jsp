<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="constants" class="com.nicologies.prbranch.SettingsBean"/>

<c:choose>
  <c:when test="${propertiesBean.properties[constants.authType] == constants.tokenAuthType}">
    <div class="parameter">
      Github Token: <strong><props:displayValue name="${constants.githubToken}" emptyValue="not specified"/></strong>
    </div>
  </c:when>
  <c:when test="${propertiesBean.properties[constants.authType] == constants.usernameAuthType">
    <div class="parameter">
      Github Username: <strong><props:displayValue name="${constants.githubUsername}" emptyValue="not specified"/></strong>
    </div>
  </c:when>
</c:choose>

<div class="parameter">
  Export branch name as configuration parameter: <strong><props:displayValue name="${constants.exportParamName}" emptyValue="not specified"/></strong>
</div>
