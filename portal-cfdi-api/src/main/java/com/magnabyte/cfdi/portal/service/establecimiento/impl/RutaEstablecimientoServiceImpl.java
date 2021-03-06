package com.magnabyte.cfdi.portal.service.establecimiento.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.magnabyte.cfdi.portal.dao.establecimiento.RutaEstablecimientoDao;
import com.magnabyte.cfdi.portal.model.establecimiento.RutaRepositorio;
import com.magnabyte.cfdi.portal.model.exception.PortalException;
import com.magnabyte.cfdi.portal.service.establecimiento.RutaEstablecimientoService;

/**
 * 
 * @author Magnabyte, S.A. de C.V
 * magnabyte.com.mx
 * Fecha:27/01/2014
 * Clase que representa el servicio de ruta de establecimiento
 */
@Service("RutaEstablecimiento")
public class RutaEstablecimientoServiceImpl implements RutaEstablecimientoService {
	
	private static final Logger logger = LoggerFactory.getLogger(RutaEstablecimientoServiceImpl.class);
	
	@Autowired
	private RutaEstablecimientoDao rutaEstablecimientoDao;
	
	@Autowired
	private MessageSource messageSource;
	
	@Transactional
	@Override
	public void save(RutaRepositorio rutaRepositorio) {
		if (rutaRepositorio != null) {
			rutaEstablecimientoDao.save(rutaRepositorio);
		} else {
			logger.error(messageSource.getMessage("ruta.establecimiento.nula", null, null));
			throw new PortalException(messageSource.getMessage("ruta.establecimiento.nula", null, null));
		}
	}
	
	@Transactional
	@Override
	public void update(RutaRepositorio rutaRepositorio) {

		if (rutaRepositorio != null) {
			rutaEstablecimientoDao.update(rutaRepositorio);

		} else {
			logger.error(messageSource.getMessage("ruta.establecimiento.nula", null, null));
			throw new PortalException(messageSource.getMessage("ruta.establecimiento.nula", null, null));
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public RutaRepositorio readById (RutaRepositorio rutaRepositorio){
		return rutaEstablecimientoDao.readById(rutaRepositorio);
	}
	

}
