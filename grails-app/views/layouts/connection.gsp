<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<title><g:layoutTitle default="Connection"/></title>
        <g:layoutHead />
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	</head>
	<body>
		<g:render template="/eyebrow"/>
		<g:render template="/settings/menu"/>
		<g:layoutBody />
	</body>
</html>