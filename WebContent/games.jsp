<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>   
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Steam Game Ratings</title>
</head>
<body>
	List of games here:
	<table>
		<c:forEach var="game" items="${games}">
			<tr>
				<td>${game.appid}</td>
				<td>${game.name}</td>
				<td>${game.positive}</td>
				<td>${game.negative}</td>
				<td>${game.rating}</td>
			</tr>
		</c:forEach>
	</table>
</body>
</html>