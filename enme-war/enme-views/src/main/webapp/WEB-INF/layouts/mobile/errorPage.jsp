<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp"%>
<%@ include file="/WEB-INF/jsp/includes/setup/web/setup_init.jsp"%>
    <div class="error">
         <spring:message code="setup.process.fail"></spring:message>
    </div>
    <c:if test="${not empty message}">
        <div class="${message.infoType.css}">${message.message}</div>
    </c:if>
<%@ include file="/WEB-INF/jsp/includes/setup/setup_finish.jsp"%>