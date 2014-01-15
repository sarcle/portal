package com.magnabyte.cfdi.portal.web.webservice;

import javax.servlet.http.HttpServletRequest;

import com.magnabyte.cfdi.portal.model.documento.Documento;

public interface DocumentoWebService {

	boolean timbrarDocumento(Documento documento, HttpServletRequest request, int idServicio);

	void recuperarAcusesPendientes();
	
	void recuperarAcuse(Documento documento);

	int obtenerIdServicio();
}