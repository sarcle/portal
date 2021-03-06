package com.magnabyte.cfdi.portal.service.commons.impl;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.magnabyte.cfdi.portal.dao.commons.OpcionDeCatalogoDao;
import com.magnabyte.cfdi.portal.model.commons.OpcionDeCatalogo;
import com.magnabyte.cfdi.portal.service.commons.OpcionDeCatalogoService;

/**
 * 
 * @author Magnabyte, S.A. de C.V
 * magnabyte.com.mx
 * Fecha:31/01/2014
 * 
 * Clase que representa el servicio de opcion de catalogo
 */
@Service("opcionDeCatalogoService")
public class OpcionDeCatalogoServiceImpl implements OpcionDeCatalogoService{

	private static final Logger logger = 
			Logger.getLogger(OpcionDeCatalogoServiceImpl.class);
	
	@Autowired
	private OpcionDeCatalogoDao opcionDeCatalogoDao;
	
	@Transactional(readOnly = true)
	@Override
	public Collection<OpcionDeCatalogo> getCatalogo(String catalogo,
			String orderBy) {
		logger.debug("Obteniendo la opcion de catalogo de la base de datos");
		return opcionDeCatalogoDao.getCatalogo(catalogo, orderBy);
	}

	@Transactional(readOnly = true)
	@Override
	public Collection<OpcionDeCatalogo> getCatalogoParam(String catalogo,
			String campo, String param, String orderBy) {
		logger.debug("Obteniendo la opcion de catalogo de la base de datos");
		return opcionDeCatalogoDao.getCatalogoParam(catalogo, campo, param, orderBy); 
	}
	
	@Transactional
	@Override
	public void save(OpcionDeCatalogo opcionDeCatalogo, String catalogo, String campoId) {
		logger.debug("Guardando la opción de catalogo en la base de datos");
		opcionDeCatalogoDao.save(opcionDeCatalogo, catalogo, campoId);
	}
}
