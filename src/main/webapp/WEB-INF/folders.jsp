<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <link href="/stylesheets/main.css" rel="stylesheet" type="text/css" />
</head>

<body>
  <header>
    <nav>
      <a href="index.html">Dashboard</a>
      <a href="search.html">Search</a>
      <a href="run.html">Run Scans</a>
      <a href="splunk.html">Splunk</a>
    </nav>
    <h1>Nessus Dashboard</h1>
  </header>

  <main>
    <h2>Folders</h2>
    <c:set var="fields" value="${folderFields}" scope="request" />
    <c:set var="entities" value="${folders}" scope="request" />
    <jsp:include page="/WEB-INF/entityTable.jsp" />

    <h2>Scans</h2>
    <c:set var="fields" value="${scanFields}" scope="request" />
    <c:set var="entities" value="${scans}" scope="request" />
    <jsp:include page="/WEB-INF/entityTable.jsp" />

  </main>
</body>
</html>