<%--
  ~ Copyright 2000-2013 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ include file="/include.jsp" %>
<%@ page import="teamcity.plugin.build.trigger.webhook.TriggerParameters" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="buildForm" scope="request" type="jetbrains.buildServer.controllers.admin.projects.EditableBuildTypeSettingsForm"/>
<jsp:useBean id="server" scope="request" type="jetbrains.buildServer.BuildServer"/>

<tr class="noBorder" >
    <td colspan="2">
        <strong>Trigger a build by sending a webhook to the following URL</strong>
        <div style="padding:0.5em; padding-left:2em;"><em>${server.rootUrl}/app/rest/webhook-trigger/${buildForm.externalId}</em></div>
    </td>
</tr>

<tr class="noBorder" >
    <th><label for="<%=TriggerParameters.PATH_MAPPINGS%>">Path Mappings: <l:star/></label></th>
    <td>
     <c:set var="note_paths">Newline-delimited set of path mappings. e.g. name=foo::path=$.foo.bar::required=true::defaultValue=bar</c:set>
     <props:multilineProperty name="<%=TriggerParameters.PATH_MAPPINGS%>" linkTitle="Edit Path Mappings" cols="35" rows="3" note="${note_paths}"/>
    </td>
</tr>
<tr class="noBorder" >
    <th><label for="<%=TriggerParameters.FILTERS%>">Filters:</label></th>
    <td>
     <c:set var="note_filters">Newline-delimited set of filters. e.g. name=branch::template=\${foo}::regex=\w</c:set>
     <props:multilineProperty name="<%=TriggerParameters.FILTERS%>" linkTitle="Edit Filters" cols="35" rows="3" note="${note_filters}"/>
    </td>
</tr>

<tr class="noBorder" >
    <th><label for="<%=TriggerParameters.INCLUDE_WHOLE_PAYLOAD%>">Include Payload: </label></th>
    <td>
       <props:checkboxProperty name="<%=TriggerParameters.INCLUDE_WHOLE_PAYLOAD%>"/>
      <span class="smallNote">
          Whether to define a build parameter named 'payload', that includes the whole webhook payload.
      </span>
        <span class="error" id="error_<%=TriggerParameters.INCLUDE_WHOLE_PAYLOAD%>"></span>
    </td>
</tr>
<tr class="noBorder" >
    <th><label for="<%=TriggerParameters.TOP_OF_QUEUE%>">Top of Queue: </label></th>
    <td>
       <props:checkboxProperty name="<%=TriggerParameters.TOP_OF_QUEUE%>"/>
      <span class="smallNote">
          Whether to move the triggered build to the top of the queue.
      </span>
        <span class="error" id="error_<%=TriggerParameters.TOP_OF_QUEUE%>"></span>
    </td>
</tr>
