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
<!-- <link rel="stylesheet" type="text/css" href="//cdn.datatables.net/1.10.7/css/jquery.dataTables.css"> -->
<link rel="stylesheet" type="text/css" href="//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="//cdn.datatables.net/plug-ins/1.10.7/integration/bootstrap/3/dataTables.bootstrap.css">

<!-- jQuery -->
<script type="text/javascript" charset="utf8" src="//code.jquery.com/jquery-1.10.2.min.js"></script>
  
<!-- DataTables -->
<script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.7/js/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf8" src="//cdn.datatables.net/plug-ins/1.10.7/integration/bootstrap/3/dataTables.bootstrap.js"></script>

<script type="text/javascript" charset="utf-8">
$(document).ready( function () {
    $('#gamesTable').DataTable( {
    	"pageLength": 50,
    	"lengthMenu": [ [10, 25, 50, -1], [10, 25, 50, "All"] ],
    	"order": [[4, 'desc'], [2, 'desc']]
    } );
} );
</script>

</head>
<body>
	<div class="container">
	List of games here:
	<table id="gamesTable" class="table table-striped table-bordered" cellspacing="0" width="100%">
		<thead>
			<tr>
				<td>App ID</td>
				<td>Name</td>
				<td>Positive (+)</td>
				<td>Negative (-)</td>
				<td>Rating (%)</td>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="game" items="${games}">
				<tr>
					<td><a href="http://store.steampowered.com/app/${game.appid}">${game.appid}</a></td>
					<td>${game.name}</td>
					<td>${game.positive}</td>
					<td>${game.negative}</td>
					<td>${game.rating}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
	</div>

	
</body>
</html>