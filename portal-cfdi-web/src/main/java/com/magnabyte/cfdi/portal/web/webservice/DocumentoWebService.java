package com.magnabyte.cfdi.portal.web.webservice;

import com.magnabyte.cfdi.portal.model.documento.Documento;

public interface DocumentoWebService {

	boolean timbrarDocumento(Documento documento, int idServicio);

	void recuperarAcusesPendientes();
	
	void recuperarAcuse(Documento documento);

	int obtenerIdServicio();
}