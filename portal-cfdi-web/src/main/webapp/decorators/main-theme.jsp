<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator"
	prefix="decorator"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta charset="utf-8">
<meta name="viewport" content="width=device-width">
<title>. : Modatelas - <decorator:title default="Main" /> : .
</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/font-awesome/css/font-awesome.min.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/bootstrap.min.css" />" />
<!-- <link rel="stylesheet" type="text/css" -->
<%-- 	href="<c:url value="/resources/css/bootstrap-theme.min.css" />" /> --%>
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/modatelas-style.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/bootstrap-datatables.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/datepicker.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/validationEngine.jquery.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/resources/css/prettyLoader.css" />" />
	
<script src="<c:url value="/resources/js/jquery/jquery-1.10.2.min.js" />"></script>
<script src="<c:url value="/resources/js/jquery/jquery.dataTables.min.js" />"></script>

<script src="<c:url value="/resources/js/jquery/jquery.validate.js" />"></script>
<script src="<c:url value="/resources/js/jquery/jquery.prettyLoader.js" />"></script>
<script src="<c:url value="/resources/js/jquery/jquery.validationEngine.js" />"></script>
<script src="<c:url value="/resources/js/jquery/jquery.validationEngine-es.js" />"></script>

<script src="<c:url value="/resources/js/datatable/datatable.js" />"></script>

<script src="<c:url value="/resources/js/datepicker/bootstrap-datepicker.js" />"></script>
<script src="<c:url value="/resources/js/datepicker/bootstrap-datepicker.es.js" />"></script>

<script type="text/javascript">
	var contextPath = "${pageContext.request.contextPath}";

	function formatDateToString(dateToFormat) {
		var today = dateToFormat; 
	    var curr_date = today.getDate();
	    var curr_month = today.getMonth() + 1;
	    var curr_year = today.getFullYear();
	    if(curr_date < 10){curr_date = '0' + curr_date} 
	    if(curr_month < 10){curr_month = '0' + curr_month}
		return curr_date + '-' + curr_month + '-' + curr_year;
	}
	
	function autoClosingAlert(selector, delay) {
		var alert = $(selector).alert();
		window.setTimeout(function() {
			alert.fadeOut("slow");
		}, delay);
	}
	
	$(function() {
		$.prettyLoader();
		autoClosingAlert("div.auto-close", 3500);
		
		$("a[href='#top']").click(function() {
			  $("html, body").animate({ scrollTop: 0 }, "slow");
			  return false;
		});
	});
	
	$(document).ready(function(){
		// fade in #back-top
		$(function () {
			$(window).scroll(function () {
				if ($(this).scrollTop() > 100) {
					$('#back-top').fadeIn();
				} else {
					$('#back-top').fadeOut();
				}
			});
	
			// scroll body to 0px on click
			$('#back-top a').click(function () {
				$('body,html').animate({
					scrollTop: 0
				}, 800);
				return false;
			});
		});
	});
</script>

<decorator:head />
</head>
<body>
	<div class="wrap">
		<!-- HEADER -->
		<div class="navbar navbar-fixed-top navbar-inverse" role="navigation">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse"
						data-target=".navbar-collapse">
						<span class="icon-bar"></span> <span class="icon-bar"></span> <span
							class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="<c:url value="/menuPage" />">
						Facturación en Línea <i class="fa fa-globe"></i>
					</a>
				</div>

				<c:url var="logoutUrl" value="/perform_logout" />
				<c:url var="catalogoEstablecimiento" value="/catalogoEstablecimiento" />
				<c:url var="catalogoUsuarios" value="/catalogoUsuarios"></c:url>
				<c:url var="catalogoClientes" value="/buscaRfc"></c:url>
				
				<div class="collapse navbar-collapse pull-right">
					<sec:authorize access="isAnonymous()">
						<c:set var="urlMenu" value="/portal/cfdi/menu"/>	
					</sec:authorize>
					<sec:authorize
							access="hasAnyRole('ROLE_SUC', 'ROLE_CORP', 'ROLE_ADMIN')">
						<c:set var="urlMenu" value="/menuPage"/>		
					</sec:authorize>
					<ul class="nav navbar-nav">
						<c:if test="${!isLoginPage}">	
							<li>
								<a href="<c:url value="${urlMenu}" />">Menú Principal
									<i class="fa fa-home"></i>
								</a>
							</li>
						</c:if>
						<li class="dropdown">
							<sec:authorize access="hasAnyRole('ROLE_ADMIN')">
								<a href="" class="dropdown-toggle" data-toggle="dropdown" >Administración <i class="fa fa-cog"></i> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="${catalogoUsuarios}"><i class="fa fa-user"></i> Usuarios</a></li>
									<li><a href="${catalogoEstablecimiento}"><i class="fa fa-dot-circle-o"></i> Sucursales</a></li>
									<li><a href="${catalogoClientes}"><i class="fa fa-users"></i> Clientes</a></li>
								</ul>
							</sec:authorize>
						</li>
					</ul>
					<sec:authorize
						access="hasAnyRole('ROLE_SUC', 'ROLE_CORP', 'ROLE_ADMIN')">
						<div class="navbar-right">
							<div class="btn-group">
								<button type="button"
									class="btn btn-warning btn-sm dropdown-toggle"
									data-toggle="dropdown">
									<i class="fa fa-user"></i> - 
									${fn:toUpperCase(sessionScope.establecimiento.nombre)} <span
										class="caret"></span>
								</button>
								<ul class="dropdown-menu" role="menu">
									<c:url var="logoutUrl" value="/perform_logout" />
									<li>
										<a href="${logoutUrl}">
											<i class="fa fa-sign-out"></i> Cerrar sesión
										</a>
									</li>
								</ul>
							</div>
						</div>
					</sec:authorize>	<div id="page_loader" class="page_loader">
	</div>
	<div class="page_loader_content text-center">
		<div class="row">
			<div class="panel col-md-4 col-md-offset-4">
				<h3>Validando Ticket</h3>
				<p>Espere por favor...<i class="fa fa-clock-o"></i></p>
				<div class="progress progress-striped active">
				  	<div class="progress-bar"  role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%"></div>
				</div>
			</div>
		</div>
	</div>
				</div>
			</div>
		</div>
		<div class="logo-header">
			<div class="container">
				<div class="row">
					<div class="col-md-3">
						<div class="logo">
							<a href="#"><img id="logoImg"
								src="<c:url value="/resources/img/modatelas_logo.jpg" />"
								alt="Logo"></a>
						</div>
					</div>
				</div>
			</div>
		</div>
		<!-- CONTENT -->
		<div class="content">
			<decorator:body />
		</div>
	</div>

	<!-- FOOTER -->
	<div id="footer" class="footer">
		<div class="row">
			<p class="credit">
				&reg; <strong>2014
					Modatelas S.A.P.I de C.V.</strong>
			</p>
			<p class="credit">
				<a href="#top"><strong> Ir arriba </strong>
					<i class="fa fa-arrow-circle-o-up"></i></a>
			</p>
			<p class="credit">
				<small><strong>Powered by <em>Magnabyte</em></strong></small>
			</p>
		</div>
	</div>
	<p id="back-top">
		<a href="#top"><span></span>Ir arriba</a>
	</p>
	<div id="page_loader" class="page_loader">
	</div>
	<div id="page_loader_factura_content" class="page_loader_content text-center">
		<div class="row">
			<div class="panel col-md-4 col-md-offset-4">
				<h3>Generando Factura</h3>
				<p>Espere por favor...<i class="fa fa-clock-o"></i></p>
				<div class="progress progress-striped active">
				  	<div class="progress-bar"  role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%"></div>
				</div>
			</div>
		</div>
	</div>
	<div id="page_loader_ticket_content" class="page_loader_content text-center">
		<div class="row">
			<div class="panel col-md-4 col-md-offset-4">
				<h3>Validando Ticket</h3>
				<p>Espere por favor...<i class="fa fa-clock-o"></i></p>
				<div class="progress progress-striped active">
				  	<div class="progress-bar"  role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%"></div>
				</div>
			</div>
		</div>
	</div>
	<script src="<c:url value="/resources/js/vendor/bootstrap.min.js" />"></script>
	<script src="<c:url value="/resources/js/vendor/bootstrap-confirmation.js" />"></script>
	<script src="<c:url value="/resources/js/vendor/modernizr-2.6.2-respond-1.1.0.min.js" />"></script>
	<script src="<c:url value="/resources/js/main/modatelas.js"/>"></script>
</body>
</html>
