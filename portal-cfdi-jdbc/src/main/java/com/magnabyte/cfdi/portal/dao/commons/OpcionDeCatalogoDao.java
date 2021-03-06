package com.magnabyte.cfdi.portal.dao.commons;

import java.util.Collection;

import com.magnabyte.cfdi.portal.model.commons.OpcionDeCatalogo;

/**
 * 
 * @author Magnabyte, S.A. de C.V
 * magnabyte.com.mx
 * Fecha:27/01/2014
 * Interfáz que representa el acceso a datos para opción de catálogo
 */
public interface OpcionDeCatalogoDao {

	Collection<OpcionDeCatalogo> getCatalogo(String catalogo, String orderBy);
	
	Collection<OpcionDeCatalogo> getCatalogoParam(String catalogo, String campo, String param, String orderBy);
	
	void save(OpcionDeCatalogo opcionDeCatalogo,String catalogo, String campoId);
}
