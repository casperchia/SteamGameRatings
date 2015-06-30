<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>   
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Steam Game Ratings</title>

<!-- DataTables CSS -->
<link rel="stylesheet" type="text/css" href="//cdn.datatables.net/1.10.7/css/jquery.dataTables.css">
  
<!-- jQuery -->
<script type="text/javascript" charset="utf8" src="//code.jquery.com/jquery-1.10.2.min.js"></script>
  
<!-- DataTables -->
<script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.7/js/jquery.dataTables.js"></script>

<script>
$(document).ready( function () {
    $('#gamesTable').DataTable();
} );
</script>

</head>
<body>
	List of games here:
	<table id="gamesTable">
		<thead>
			<tr>
				<td>App ID</td>
				<td>Name</td>
				<td>Negative (-)</td>
				<td>Positive (+)</td>
				<td>Rating (%)</td>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="game" items="${games}">
				<tr>
					<td>${game.appid}</td>
					<td>${game.name}</td>
					<td>${game.positive}</td>
					<td>${game.negative}</td>
					<td>${game.rating}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</body>
</html>