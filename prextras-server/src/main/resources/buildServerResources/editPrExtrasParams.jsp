<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="settings" class="com.nicologies.prextras.SettingsBean"/>

<c:if test="${settings.tokenAuthType == propertiesBean.properties[settings.authType]}">
  <c:set var="hideAuthByUsername" value="style='display: none'"/>
</c:if>

<c:if test="${settings.usernameAuthType == propertiesBean.properties[settings.authType]}">
  <c:set var="hideAuthByToken" value="style='display: none'"/>
</c:if>

<c:if test="${settings.systemWideTokenAuthType == propertiesBean.properties[settings.authType]}">
  <c:set var="hideAuthByToken" value="style='display: none'"/>
  <c:set var="hideAuthByUsername" value="style='display: none'"/>
</c:if>

<l:settingsGroup title="Settings">
  <tr>
    <th><label>Github Authorisation Type: </label></th>
    <td>
        <c:set var="onclickAuthByToken">
          BS.Util.hide('usernameAuthSection');
          BS.Util.show('tokenSection');
          BS.Util.hide('passwordAuthSection');
        </c:set>

        <props:radioButtonProperty name="${settings.authType}"
                                 id="authTypeToken"
                                 value="${settings.tokenAuthType}"
                                 checked="${settings.tokenAuthType == propertiesBean.properties[settings.authType]}"
                                 onclick="${onclickAuthByToken}" />
        <label for="authTypeToken">Token</label>

      <span style="padding-left: 5em">
        <c:set var="onclickAuthByUsername">
          BS.Util.show('usernameAuthSection');
          BS.Util.hide('tokenSection');
          BS.Util.show('passwordAuthSection');
        </c:set>
        <props:radioButtonProperty name="${settings.authType}"
                                   id="usernameAuthType"
                                   value="${settings.usernameAuthType}"
                                   checked="${settings.usernameAuthType == propertiesBean.properties[settings.authType]}"
                                   onclick="${onclickAuthByUsername}"/>
        <label for="usernameAuthType">Github Account</label>
      </span>
       <span style="padding-left: 5em">
              <c:set var="onclickAuthBySystemWideToken">
                BS.Util.hide('usernameAuthSection');
                BS.Util.hide('tokenSection');
                BS.Util.hide('passwordAuthSection');
              </c:set>
              <props:radioButtonProperty name="${settings.authType}"
                                         id="systemWideTokenAuthType"
                                         value="${settings.systemWideTokenAuthType}"
                                         checked="${settings.systemWideTokenAuthType == propertiesBean.properties[settings.authType]}"
                                         onclick="${onclickAuthBySystemWideToken}"/>
              <label for="systemWideTokenAuthType">System Wide Token(system.${settings.githubToken})</label>
            </span>
    </td>
  </tr>

  <tr id="tokenSection" ${hideAuthByToken}>
    <th><label for="${settings.githubToken}">Github Token: </label></th>
    <td><props:textProperty name="${settings.githubToken}" className='longField'/>
      <span class="error" id="error_${settings.githubToken}"></span>
      <span class="smallNote">Optional for public repository. Follow the instructions <a href='https://help.github.com/articles/creating-an-access-token-for-command-line-use/'>here</a> to create a token.</span>
    </td>
  </tr>

  <tr id="usernameAuthSection" ${hideAuthByUsername}>
    <th><label for="${settings.githubUsername}">Github Username: </label></th>
    <td>
      <props:textProperty name="${settings.githubUsername}" className='longField'/>
      <span class="error" id="error_${settings.githubUsername}"></span>
      <span class="smallNote">Optional for public repository</span>
    </td>
  </tr>
  <tr id="passwordAuthSection" ${hideAuthByUsername}>
    <th><label for="${settings.githubPassword}">Github Password: </label></th>
    <td>
      <props:passwordProperty name="${settings.githubPassword}" className='longField'/>
      <span class="error" id="error_${settings.githubPassword}"></span>
      <span class="smallNote">Optional for public repository</span>
    </td>
  </tr>

  <tr>
    <th><label for="${settings.exportParamName}">Export branch name to a configuration parameter</label></th>
    <td><props:textProperty name="${settings.exportParamName}" className="longField"/>
      <span class="error" id="error_${settings.exportParamName}"></span>
      <span class="smallNote">Export the branch name as a teamcity configuration parameter, so you can use it later in the build process</span>
    </td>
  </tr>

  <tr>
    <th><label for="${settings.appendToBuildNum}">Append the branch name to the build num label</label></th>
    <td>
      <props:checkboxProperty name="${settings.appendToBuildNum}"/>
      <span class="error" id="error_${settings.appendToBuildNum}"></span>
    </td>
  </tr>

  <tr>
    <th><label for="${settings.failBuildIfConflict}">Fail the build if the pull request has conflicts</label></th>
    <td>
      <props:checkboxProperty name="${settings.failBuildIfConflict}"/>
      <span class="error" id="error_${settings.failBuildIfConflict}"></span>
    </td>
  </tr>
</l:settingsGroup>
