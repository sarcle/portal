<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:if test="${!emptyList}">
	<c:choose>
		<c:when test="${not empty documentos}">
			<div class="table-responsive">
				<display:table htmlId="documentos" id="documento" name="${documentos}"
					class="table table-hover table-striped table-condensed"
					requestURI="">
					<display:column title="#" property="id" headerClass="text-primary"></display:column>
					<display:column title="Nombre Documento" property="nombre" headerClass="text-primary text-center" class="text-center" />
<%-- 					<display:column title="Serie" property="comprobante.serie" headerClass="text-primary"></display:column> --%>
<%-- 					<display:column title="Folio" property="comprobante.folio" headerClass="text-primary" /> --%>
					<display:column title="PDF" headerClass="text-primary text-center" class="text-center">
						<button type="button" id="descargaDocPdf" class="btn btn-xs btn-danger">Descargar <span class="glyphicon glyphicon-download-alt"></span></button>
					</display:column>
					<display:column title="XML" headerClass="text-primary text-center" class="text-center">
						<button type="button" id="descargaDocXml" class="btn btn-xs btn-primary">Descargar <span class="glyphicon glyphicon-download-alt"></span></button>
					</display:column>
					<display:column title="Reenviar" headerClass="text-primary text-center" class="text-center">
						<button type="button" id="reenviarDocumento" class="btn btn-xs btn-primary" data-toggle="modal" data-target="#myModal">Reenviar <span class="glyphicon glyphicon-envelope"></span></button>
					</display:column>
				</display:table>
			</div>
			<c:url value="/documentoDownload/${documento.establecimiento.id}/${documento.nombre}/pdf" var="urlDocumentoPdf"/>
			<c:url value="/documentoDownload/${documento.establecimiento.id}/${documento.nombre}/xml" var="urlDocumentoXml"/>
			<form id="formDocument" method="post"></form>
			<script type="text/javascript">
				$(document).ready(function() {
					$("#reenvioDocForm").validationEngine();
					
					var urlPdf = "${urlDocumentoPdf}";
					var urlXml = "${urlDocumentoXml}";
						
					$("#descargaDocPdf").click(function() {
						$("#formDocument").attr("action", urlPdf);
						$("#formDocument").submit();
					});
					
					$("#descargaDocXml").click(function() {
						alert("Funciona XML!");
						$("#formDocument").attr("action", urlXml);
						$("#formDocument").submit();
					});
					
					$("#enviaMail").click(function() {
						if($("#reenvioDocForm").validationEngine("validate")){
							alert("valido..");
						}
					});
				});
			</script>
		</c:when>
		<c:otherwise>
			<div class="form-group">
				<div class="centered">
					<p class="alert alert-info">El RFC proporcionado no tiene ninguna factura relacionada.</p>
				</div>
			</div>
		</c:otherwise>		
	</c:choose>
</c:if>
<script src="<c:url value="/resources/js/documento/documento.js" />"></script>

<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" id="close" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title text-primary">Reenviar Documentos</h4>
      </div>
      <div class="modal-body">
        <form action="/reenvioDocuemnto" id="reenvioDocForm" method="post" class="form-horizontal">
        	<blockquote>
        		<p>Proporcione un correo electronico para reenviar los documentos.</p>
        	</blockquote>
        	<div class="form-group">
				<label for="email" class="col-lg-4 control-label">Email: </label>
				<div class="col-lg-5">
		        	<input type="text" id="email" name="email" class="form-control input-sm validate[required, custom[email]] noUpper"/>
				</div>
			</div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button>
        <button type="button" id="enviaMail" class="btn btn-primary">Enviar</button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div>
