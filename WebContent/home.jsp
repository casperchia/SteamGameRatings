<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="CSS/main.css">
<title>Steam Game Ratings</title>
</head>
<body>

	Hello World!
	<form action="Control" method="GET">
		<input type="hidden" name="action" value="login">
		<input class="steamLoginBtn" type="submit" value="">
	</form>
	
	<form action="Control" method="GET" id="searchForm">
		<input type="text" name="username" maxlength="50"/>
		<input type="hidden" name="action" value="gamesRequest"/>
		<input type="submit" value="Enter"/>
	</form>
	

</body>
</html>
