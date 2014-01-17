package com.magnabyte.cfdi.portal.service.samba;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.NtlmPasswordAuthentication;

import com.magnabyte.cfdi.portal.model.documento.Documento;
import com.magnabyte.cfdi.portal.model.documento.DocumentoCorporativo;
import com.magnabyte.cfdi.portal.model.establecimiento.Establecimiento;

public interface SambaService {

	InputStream getFileStream(String url, String fileName, NtlmPasswordAuthentication authentication);

	List<DocumentoCorporativo> getFilesFromDirectory(String url, NtlmPasswordAuthentication authentication);

	void moveProcessedSapFile(DocumentoCorporativo documento);

	void writeProcessedCfdiXmlFile(byte[] xmlCfdi, Documento documento);

	void writePdfFile(Documento documento, HttpServletRequest request);

	void writeAcuseCfdiXmlFile(byte[] acuseCfdi, Documento documento);

	NtlmPasswordAuthentication getAuthentication(Establecimiento establecimiento);

}
