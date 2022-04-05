<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="data-main" style="grid-template-columns: repeat(${fields.size()}, auto);">
  <c:forEach var="fieldName" items="${fields}">
    <div class="data-header"><c:out value="${fieldName}" /></div>
  </c:forEach>

  <c:forEach var="entity" items="${entities}">
    <c:set var="node" value="${entity.toJsonNode()}" />

    <c:forEach var="fieldName" items="${fields}">

      <c:set var="val" value="${node.get(fieldName)}" />

      <c:choose>
        <c:when test="${val == null}">
          <div class="empty-cell"></div>
        </c:when>
        <c:otherwise>
          <div class="data-cell"><c:out value="${val.toString()}" /></div>
        </c:otherwise>
      </c:choose>

    </c:forEach>
  </c:forEach>
</div>