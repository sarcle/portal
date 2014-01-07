package com.magnabyte.cfdi.portal.service.documento.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import mx.gob.sat.cfd._3.Comprobante;
import mx.gob.sat.cfd._3.Comprobante.Conceptos;
import mx.gob.sat.cfd._3.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd._3.Comprobante.Emisor;
import mx.gob.sat.cfd._3.Comprobante.Impuestos;
import mx.gob.sat.cfd._3.Comprobante.Receptor;
import mx.gob.sat.cfd._3.TUbicacion;

import org.apache.commons.ssl.PKCS8Key;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.magnabyte.cfdi.portal.dao.documento.DocumentoDao;
import com.magnabyte.cfdi.portal.dao.documento.DocumentoSerieDao;
import com.magnabyte.cfdi.portal.dao.documento.sql.DocumentoSql;
import com.magnabyte.cfdi.portal.dao.emisor.EmisorDao;
import com.magnabyte.cfdi.portal.model.cliente.Cliente;
import com.magnabyte.cfdi.portal.model.cliente.DomicilioCliente;
import com.magnabyte.cfdi.portal.model.commons.Estado;
import com.magnabyte.cfdi.portal.model.commons.Pais;
import com.magnabyte.cfdi.portal.model.documento.Documento;
import com.magnabyte.cfdi.portal.model.documento.DocumentoCorporativo;
import com.magnabyte.cfdi.portal.model.documento.DocumentoSucursal;
import com.magnabyte.cfdi.portal.model.emisor.EmpresaEmisor;
import com.magnabyte.cfdi.portal.model.establecimiento.Establecimiento;
import com.magnabyte.cfdi.portal.model.establecimiento.factory.EstablecimientoFactory;
import com.magnabyte.cfdi.portal.model.exception.PortalException;
import com.magnabyte.cfdi.portal.model.ticket.Ticket;
import com.magnabyte.cfdi.portal.model.ticket.Ticket.Transaccion.InformacionPago;
import com.magnabyte.cfdi.portal.model.ticket.Ticket.Transaccion.Partida;
import com.magnabyte.cfdi.portal.model.ticket.Ticket.Transaccion.PartidaDescuento;
import com.magnabyte.cfdi.portal.service.cliente.ClienteService;
import com.magnabyte.cfdi.portal.service.cliente.DomicilioClienteService;
import com.magnabyte.cfdi.portal.service.commons.EmailService;
import com.magnabyte.cfdi.portal.service.commons.OpcionDeCatalogoService;
import com.magnabyte.cfdi.portal.service.documento.DocumentoDetalleService;
import com.magnabyte.cfdi.portal.service.documento.DocumentoService;
import com.magnabyte.cfdi.portal.service.documento.TicketService;
import com.magnabyte.cfdi.portal.service.establecimiento.EstablecimientoService;
import com.magnabyte.cfdi.portal.service.samba.SambaService;
import com.magnabyte.cfdi.portal.service.xml.DocumentoXmlService;
import com.magnabyte.cfdi.portal.service.xml.util.CfdiConfiguration;

@Service("documentoService")
public class DocumentoServiceImpl implements DocumentoService, ResourceLoaderAware {

	private static final Logger logger = LoggerFactory.getLogger(DocumentoServiceImpl.class);
	
	@Autowired
	private DocumentoXmlService documentoXmlService;
	
	@Autowired
	private EmisorDao emisorDao;
	
	@Autowired
	private DocumentoDao documentoDao;
	
	@Autowired
	private DocumentoSerieDao documentoSerieDao;
	
	@Autowired
	private DocumentoDetalleService documentoDetalleService;
	
	@Autowired
	private DomicilioClienteService domicilioClienteService;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private OpcionDeCatalogoService opcionDeCatalogoService;
	
	@Autowired
	private EstablecimientoService establecimientoService;
	
	@Autowired
	private SambaService sambaService;
	
	@Autowired
	private EmailService emailService;

	@Autowired
	private CfdiConfiguration cfdiConfiguration;
	
	private ResourceLoader resourceLoader;
	
	@Override
	public boolean sellarComprobante(Comprobante comprobante) {
		logger.debug("en sellar Documento");
		String cadena = obtenerCadena(comprobante);
		String sello = obtenerSelloDigital(cadena);
		logger.debug("SELLO: {}", sello);
		logger.debug("CADENA: {}", cadena);
		if(validSelloDigital(sello, cadena, comprobante)) {
			comprobante.setSello(sello);
			return true;
		}
		return false;
	}

	private boolean validSelloDigital(String sello, String cadena, Comprobante comprobante) {
		CertificateFactory certFactory;
		try {
			logger.debug("validando sello...");
			certFactory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(resourceLoader.getResource("classpath:/aaa010101aaa__csd_01.cer").getInputStream());
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			comprobante.setNoCertificado(new String(certificate.getSerialNumber().toByteArray()));
			comprobante.setCertificado(new String(Base64.encode(certificate.getEncoded())));
			
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify(publicKey);
			signature.update(cadena.getBytes("UTF-8"));
			
			return signature.verify(Base64.decode(sello));
		} catch (CertificateException e) {
			logger.error("Ocurrió un error al obtener la fecha del ticket: ", e);
			throw new PortalException("Ocurrió un error al obtener la fecha del ticket: ", e);
		} catch (IOException e) {
			logger.error("Ocurrió un error al validar el Sello Digital, no se pudo cargar el certificado.", e);
			throw new PortalException("Ocurrió un error al validar el Sello Digital, no se pudo cargar el certificado.", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Ocurrió un error al validar el Sello Digital.", e);
			throw new PortalException("Ocurrió un error al validar el Sello Digital.", e);
		} catch (InvalidKeyException e) {
			logger.error("Ocurrió un error al validar el Sello Digital, el certificado es invalido", e);
			throw new PortalException("Ocurrió un error al validar el Sello Digital, el certificado es invalido", e);
		} catch (SignatureException e) {
			logger.error("Ocurrió un error al validar el Sello Digital, el certificado es invalido", e);
			throw new PortalException("Ocurrió un error al validar el Sello Digital, el certificado es invalido", e);
		}
	}

	private String obtenerSelloDigital(String cadena) {
		try {
			logger.debug("en obtener sello digital");
			InputStream keyStream = resourceLoader.getResource("classpath:/aaa010101aaa__csd_01.key").getInputStream();
			PKCS8Key key = new PKCS8Key(keyStream, "12345678a".toCharArray());
			PrivateKey privateKey = key.getPrivateKey();
			
			Signature signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privateKey);
			signature.update(cadena.getBytes("UTF-8"));
			byte[] firma = signature.sign();
			logger.debug("regresando sello");
			return new String(Base64.encode(firma));
		} catch (IOException e) {
			logger.error("Ocurrió un error al obtener el Sello Digital, no se pudo cargar la llave del certificado.", e);
			throw new PortalException("Ocurrió un error al obtener el Sello Digital, no se pudo cargar la llave del certificado.", e);
		} catch (GeneralSecurityException e) {
			logger.error("Ocurrió un error al obtener el Sello Digital.", e);
			throw new PortalException("Ocurrió un error al obtener el Sello Digital.", e);
		}
	}

	private String obtenerCadena(Comprobante comprobante) {
		try {
			logger.debug("en obtener Cadena");
			Source xmlSource = new StreamSource(documentoXmlService.convierteComprobanteAStream(comprobante));
			Source xsltSource = new StreamSource(resourceLoader.getResource("classpath:/cadenaoriginal_3_2.xslt").getInputStream());
			StringWriter writer = new StringWriter();
			Result outputTarget = new StreamResult(writer);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(xsltSource);
			transformer.transform(xmlSource, outputTarget);
			logger.debug("regresando Cadena");
			return writer.toString();
		} catch (TransformerConfigurationException e) {
			logger.error("Ocurrió un error al obtener la Cadena Original.", e);
			throw new PortalException("Ocurrió un error al obtener la Cadena Original.", e);
		} catch (IOException e) {
			logger.error("Ocurrió un error al obtener la Cadena Original, "
					+ "no se pudo recuperar el xslt para generar la cadena original", e);
			throw new PortalException("Ocurrió un error al obtener la Cadena Original, "
					+ "no se pudo recuperar el xslt para generar la cadena original", e);
		} catch (TransformerException e) {
			logger.error("Ocurrió un error al obtener la Cadena Original.", e);
			throw new PortalException("Ocurrió un error al obtener la Cadena Original.", e);
		}
	}
	
	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Transactional(readOnly = true)
	@Override
	public Comprobante obtenerComprobantePor(Cliente cliente, Ticket ticket,
		Integer idDomicilioFiscal, Establecimiento establecimiento) {
		Comprobante comprobante = new Comprobante();
		
		if (ticket != null && ticket.getTransaccion().getTransaccionHeader().getIdTicket() != null &&
				ticket.getTransaccion().getTransaccionHeader().getIdCaja() != null &&
				ticket.getTransaccion().getTransaccionHeader().getIdSucursal() != null &&
				ticket.getTransaccion().getTransaccionHeader().getFechaHora() != null) {
		
			
			inicializaComprobante(comprobante, ticket);
			formatTicketDate(ticket);
			comprobante.setEmisor(getEmisorPorEstablecimiento(establecimiento));
			comprobante.setReceptor(createReceptor(cliente, idDomicilioFiscal));
			getDetalleFromTicket(ticket, comprobante);
			createFechaDocumento(comprobante);
			comprobante.setLugarExpedicion(comprobante.getEmisor().getExpedidoEn().getLocalidad());
			
			comprobante.setTipoDeComprobante("ingreso");
			comprobante.setTipoCambio("1");
			comprobante.setCondicionesDePago("PAGO DE CONTADO");
			comprobante.setFormaDePago("PAGO EN UNA SOLA EXHIBICION");
		} else {
			throw new PortalException("El ticket no puedo ser nulo.");
		}
		
		return comprobante;
	}

	private void formatTicketDate(Ticket ticket) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat sdfOrigen = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			
			Date fechaTicket = sdfOrigen.parse(ticket.getTransaccion().getTransaccionHeader().getFechaHora());
			ticket.getTransaccion().getTransaccionHeader().setFechaHora(sdf.format(fechaTicket));
		} catch (ParseException e) {
			try {
				sdf.parse(ticket.getTransaccion().getTransaccionHeader().getFechaHora());
			} catch (ParseException e1) {
				logger.error("Ocurrió un error al obtener la fecha del ticket: ", e);
				throw new PortalException("Ocurrió un error al obtener la fecha del ticket: ", e);
			}
		} 
	}

	private void inicializaComprobante(Comprobante comprobante, Ticket ticket) {
		comprobante.setVersion(cfdiConfiguration.getVersionCfdi());
		comprobante.setSello(cfdiConfiguration.getSelloPrevio());
		comprobante.setNoCertificado(cfdiConfiguration.getNumeroCertificadoPrevio());
		comprobante.setCertificado(cfdiConfiguration.getCertificadoPrevio());
		for(InformacionPago infoPago : ticket.getTransaccion().getInformacionPago()) {
			comprobante.setNumCtaPago(infoPago.getNumeroCuenta());
			comprobante.setMetodoDePago(infoPago.getPago().getMetodoPago().toUpperCase());
			comprobante.setMoneda(infoPago.getPago().getMoneda());
		}
	}

	private Emisor getEmisorPorEstablecimiento(Establecimiento establecimiento) {
		EmpresaEmisor empresaEmisor = emisorDao.read(establecimiento.getEmpresaEmisor());
		TUbicacion expedidoEn = emisorDao.readLugarExpedicion(establecimiento);
		if (expedidoEn.getNoExterior() != null && expedidoEn.getNoExterior().trim().isEmpty()) 
			expedidoEn.setNoExterior(null);
		if (expedidoEn.getNoInterior() != null && expedidoEn.getNoInterior().trim().isEmpty()) {
			expedidoEn.setNoInterior(null);
		}
		
		if (empresaEmisor.getEmisor().getDomicilioFiscal().getNoInterior() != null
				&& empresaEmisor.getEmisor().getDomicilioFiscal().getNoInterior().trim().isEmpty()) {
			empresaEmisor.getEmisor().getDomicilioFiscal().setNoInterior(null);
		}
		
		empresaEmisor.getEmisor().setExpedidoEn(expedidoEn);
		return empresaEmisor.getEmisor();
	}
	
	private Receptor createReceptor(Cliente cliente, Integer idDomicilioFiscal) {
		Receptor receptor = new Receptor();
		TUbicacion tUbicacion = new TUbicacion();
		DomicilioCliente domicilioCte = new DomicilioCliente();
		domicilioCte.setId(idDomicilioFiscal);
		
		for(DomicilioCliente domicilio : cliente.getDomicilios()) {
			if(domicilioCte.equals(domicilio)){
				domicilioCte = domicilio;
				break;
			}
		}
		
		if (domicilioCte.getCalle() != null) 
			tUbicacion.setCalle(domicilioCte.getCalle());
		if (domicilioCte.getNoExterior() != null) 
			tUbicacion.setNoExterior(domicilioCte.getNoExterior());
		if (domicilioCte.getNoInterior() != null && !domicilioCte.getNoInterior().trim().isEmpty()) {
			tUbicacion.setNoInterior(domicilioCte.getNoInterior());
		}
		if (domicilioCte.getEstado() != null) {
			tUbicacion.setPais(domicilioCte.getEstado().getPais().getNombre());
			tUbicacion.setEstado(domicilioCte.getEstado().getNombre());
		}
		if (domicilioCte.getMunicipio() != null) 
			tUbicacion.setMunicipio(domicilioCte.getMunicipio());
		if (domicilioCte.getColonia() != null) 
			tUbicacion.setColonia(domicilioCte.getColonia());
		if (domicilioCte.getCodigoPostal() != null) 
			tUbicacion.setCodigoPostal(domicilioCte.getCodigoPostal());
//		tUbicacion.setReferencia(domicilioCte.getReferencia());
//		tUbicacion.setLocalidad(domicilioCte.getLocalidad());
		
		receptor.setRfc(cliente.getRfc());
		receptor.setNombre(cliente.getNombre());
		receptor.setDomicilio(tUbicacion);
		return receptor;
	}
	
	private void getDetalleFromTicket(Ticket ticket, Comprobante comprobante) {
		BigDecimal IVA = new BigDecimal(1.16);
		Conceptos conceptos = new Conceptos();
		BigDecimal subTotal = new BigDecimal(0);
		for(Partida partida : ticket.getTransaccion().getPartidas()) {
			if (!isArticuloSinPrecio(partida.getArticulo().getId())) {
				Concepto concepto = new Concepto();
				concepto.setCantidad(partida.getCantidad());
				concepto.setDescripcion(partida.getArticulo().getDescripcion());
				concepto.setUnidad(partida.getArticulo().getUnidad());
				concepto.setImporte(partida.getPrecioTotal().divide(IVA, 2, BigDecimal.ROUND_HALF_UP));
				concepto.setValorUnitario(partida.getPrecioUnitario().divide(IVA, 2, BigDecimal.ROUND_HALF_UP));
				if (!partida.getArticulo().getTipoCategoria().equals("PROMOCIONES")) {
					subTotal = subTotal.add(concepto.getImporte());
				}
				conceptos.getConcepto().add(concepto);
			}
		}
		comprobante.setConceptos(conceptos);
		BigDecimal descuentoTotal = new BigDecimal(0);
		for(PartidaDescuento descuento : ticket.getTransaccion().getPartidasDescuentos()) {
			descuentoTotal = descuentoTotal.add(descuento.getDescuentoTotal());
		}
		
		descuentoTotal = descuentoTotal.multiply(new BigDecimal(-1));
		comprobante.setDescuento(descuentoTotal.divide(IVA, 2, BigDecimal.ROUND_HALF_UP));

		comprobante.setSubTotal(subTotal.setScale(2, BigDecimal.ROUND_HALF_UP));
		Impuestos impuesto = new Impuestos();
		impuesto.setTotalImpuestosTrasladados((comprobante.getSubTotal().subtract(comprobante.getDescuento())).multiply(IVA.subtract(new BigDecimal(1)).setScale(2, BigDecimal.ROUND_HALF_UP)));
		comprobante.setImpuestos(impuesto);
		comprobante.setTotal(comprobante.getSubTotal().subtract(comprobante.getDescuento()).add(comprobante.getImpuestos().getTotalImpuestosTrasladados()).setScale(2, BigDecimal.ROUND_UP));
	}
	
	private boolean isArticuloSinPrecio(String claveArticulo) {
		List<String> articulosSinPrecio = ticketService.readArticulosSinPrecio();
		if (articulosSinPrecio != null) {
			return articulosSinPrecio.contains(claveArticulo);
		}
		return false;
	}

	private void createFechaDocumento(Comprobante comprobante) {
		GregorianCalendar dateNow = new GregorianCalendar();
		dateNow.setTime(new Date());
		try {
			XMLGregorianCalendar fechaComprobante = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateNow.get(Calendar.YEAR), 
					dateNow.get(Calendar.MONTH) + 1, dateNow.get(Calendar.DAY_OF_MONTH), 
					dateNow.get(Calendar.HOUR), dateNow.get(Calendar.MINUTE), dateNow.get(Calendar.SECOND), 
					DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED); 
			comprobante.setFecha(fechaComprobante);
		} catch (DatatypeConfigurationException e) {
			logger.error("Ocurrió un error al asignar la fecha del Documento.", e);
			throw new PortalException("Ocurrió un error al asignar la fecha del Documento.", e);
		}
	}

	@Transactional
	@Override
	public void insertDocumentoCfdi(Documento documento) {
		documentoDao.insertDocumentoCfdi(documento);
	}
	
	@Transactional
	@Override
	public void guardarDocumento(Documento documento) {
		if(documento != null) {
			if(documento instanceof DocumentoSucursal) {
				Ticket ticketDB = ticketService.read(((DocumentoSucursal) documento).getTicket(), documento.getEstablecimiento());
				if (ticketDB != null) {
					switch (ticketDB.getTipoEstadoTicket()) {
					case GUARDADO:
						logger.debug("El ticket ya fue guardado previamente.");
						((DocumentoSucursal) documento).getTicket().setId(ticketDB.getId());
						documento.setId(documentoDao.readIdByTicket((DocumentoSucursal) documento));
						documentoDao.updateDocumentoTicket((DocumentoSucursal) documento);
						Map<String, Object> serieFolioMap = documentoSerieDao.readSerieAndFolioDocumento(documento);
						documento.getComprobante().setSerie((String) serieFolioMap.get(DocumentoSql.SERIE));
						documento.getComprobante().setFolio((String) serieFolioMap.get(DocumentoSql.FOLIO));
						break;
					case FACTURADO:
						logger.debug("El ticket ya fue facturado.");
						throw new PortalException("El ticket ya fue facturado con anterioridad.");
					default:
						break;
					}
				} else {
					ticketService.save((DocumentoSucursal) documento);
					documentoDao.save(documento);
					documentoDetalleService.save(documento);
					
					synchronized (documentoSerieDao) {
						Map<String, Object> serieFolioMap = documentoSerieDao.readSerieAndFolio(documento);
						documento.getComprobante().setSerie((String) serieFolioMap.get(DocumentoSql.SERIE));
						documento.getComprobante().setFolio((String) serieFolioMap.get(DocumentoSql.FOLIO_CONSECUTIVO));
						documentoSerieDao.updateFolioSerie(documento);
						documentoDao.insertDocumentoFolio(documento);
					}
				}
			} else if (documento instanceof DocumentoCorporativo) {
				documentoDao.save(documento);
				documentoDetalleService.save(documento);
				documentoDao.insertDocumentoFolio(documento);
			}
		} else {
			logger.debug("El Documento no puede ser nulo.");
			throw new PortalException("El Documento no puede ser nulo.");
		}		
		
	}
	
	@Transactional
	@Override
	public void insertAcusePendiente(Documento documento) {
		documentoDao.insertAcusePendiente(documento);
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<Documento> obtenerAcusesPendientes() {
		return documentoDao.obtenerAcusesPendientes();
	}
	
	@Transactional
	@Override
	public void deleteFromAcusePendiente(Documento documento) {
		documentoDao.deleteFromAcusePendiente(documento);
	}
	
	@Override
	public Cliente obtenerClienteDeComprobante(Comprobante comprobante) {
		Cliente cliente = new Cliente();
		DomicilioCliente domicilio = new DomicilioCliente();
		List<DomicilioCliente> domicilios = new ArrayList<DomicilioCliente>();
		
		Pais pais = new Pais();
		pais.setNombre(comprobante.getReceptor().getDomicilio().getPais());
		
		Estado estado = new Estado();
		estado.setNombre(comprobante.getReceptor().getDomicilio().getEstado());
		
		Pais paisBD;
		boolean paisSinEstado = false;
		
		cliente.setNombre(comprobante.getReceptor().getNombre());
		cliente.setRfc(comprobante.getReceptor().getRfc());
		
		if(!clienteService.exist(cliente)) {
			clienteService.saveClienteCorporativo(cliente);
		} else {
			cliente = clienteService.readClientesByNameRfc(cliente);
		}
		
		if(!pais.getNombre().isEmpty()) {
			paisBD = domicilioClienteService.readPais(pais);
			if(paisBD == null) {
				opcionDeCatalogoService.save(pais, "c_pais", "id_pais");
				estado.setPais(pais);
				if(!comprobarEstado(domicilio, estado)) {					
					paisSinEstado = true;
					domicilio.setEstado(new Estado());					
				}
			} else {
				estado.setPais(paisBD);				
				comprobarEstado(domicilio, estado);
			}
		}
			
		domicilio.setCalle(comprobante.getReceptor().getDomicilio().getCalle());
		domicilio.setNoExterior(comprobante.getReceptor().getDomicilio().getNoExterior());
		domicilio.setNoInterior(comprobante.getReceptor().getDomicilio().getNoInterior());
		domicilio.setColonia(comprobante.getReceptor().getDomicilio().getColonia());
		domicilio.setMunicipio(comprobante.getReceptor().getDomicilio().getMunicipio());
//		domicilio.setLocalidad(comprobante.getReceptor().getDomicilio().getLocalidad());
//		domicilio.setReferencia(comprobante.getReceptor().getDomicilio().getReferencia());
		domicilio.setCodigoPostal(comprobante.getReceptor().getDomicilio().getCodigoPostal());
		domicilios.add(domicilio);
		cliente.setDomicilios(domicilios);
		
		if(paisSinEstado) {
			domicilioClienteService.save(cliente);
			domicilioClienteService.savePaisSinEstado(cliente.getDomicilios().get(0), pais);
		} else {
			DomicilioCliente dom = cliente.getDomicilios().get(0);
			if(dom != null) {
				List<DomicilioCliente> domiciliosBD = 
						domicilioClienteService.getByCliente(cliente);
				if(domiciliosBD != null && !domiciliosBD.isEmpty()) {
					boolean existeDom = false;
					for(DomicilioCliente domicilioBD : domiciliosBD) {
						if(clienteService.comparaDirecciones(dom, domicilioBD)) {
							existeDom = true;
							break;
						}					
					}
					if(!existeDom) {
						domicilioClienteService.save(cliente);
					}
				} else {
					domicilioClienteService.save(cliente);
				}
			}
		}
		
		return cliente;
	}

	private boolean comprobarEstado(DomicilioCliente domicilio, Estado estado) {
		Estado estadoBD;
		if(estado != null && !estado.getNombre().isEmpty()) {
			estadoBD = domicilioClienteService.readEstado(estado);
			if(estadoBD == null) {
				domicilioClienteService.saveEstado(estado);
				domicilio.setEstado(estado);
			} else {
				domicilio.setEstado(estadoBD);
			}
			return true;
		}
		return false;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Documento> getDocumentos(Cliente cliente) {
		List<Documento> listaDocumentos = documentoDao.getDocumentoByCliente(cliente);
		List<Integer> idDocumentos = new ArrayList<Integer>();
		List<Documento> documentosPorId = null;
		
		if(listaDocumentos != null && !listaDocumentos.isEmpty()) {			
			for(Documento ruta : listaDocumentos) {
				idDocumentos.add(ruta.getId());
			}
		
			documentosPorId = documentoDao.getNombreDocumentoFacturado(idDocumentos);
			
			for (Documento documento2 : documentosPorId) {
				for (Documento documento : listaDocumentos) {
					if (documento2.getId().equals(documento.getId())) {
						documento2.setEstablecimiento(documento.getEstablecimiento());
						documento2.setNombre(documento2.getTipoDocumento()
								+ "_" + documento2.getComprobante().getSerie() 
								+ "_" + documento2.getComprobante().getFolio());
						break;
					}
				}
			}
		}
		
		return documentosPorId;
	}
	
	public byte[] recuperarDocumentoArchivo(String fileName, 
			Integer idEstablecimiento, String extension) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			Establecimiento estab = establecimientoService.readById(
					EstablecimientoFactory.newInstance(idEstablecimiento));
			//FIXME Cambiar la ruta out
			InputStream file = sambaService.getFileStream(estab.getRutaRepositorio().getRutaRepositorio() 
					+ estab.getRutaRepositorio().getRutaRepoOut(), fileName + "." + extension);
			
			int nRead;
			byte[] data = new byte[16384];
	
			while ((nRead = file.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}
	
			buffer.flush();
	
			return buffer.toByteArray();
		
		} catch (Exception e) {
			logger.error("Error al convertir el documento a bytes");
			throw new PortalException("Error al convertir el documento a bytes");
		}
	}

	@Override
	public void envioDocumentosFacturacion(String para, String fileName,
			Integer idEstablecimiento) {
		try {
		
			Establecimiento estab = establecimientoService.readById(
					EstablecimientoFactory.newInstance(idEstablecimiento));
			
			InputStream pdf = sambaService.getFileStream(estab.getRutaRepositorio().getRutaRepositorio() 
					+ estab.getRutaRepositorio().getRutaRepoOut(), fileName + ".pdf");
			
			InputStream xml = sambaService.getFileStream(estab.getRutaRepositorio().getRutaRepositorio() 
					+ estab.getRutaRepositorio().getRutaRepoOut(), fileName + ".xml");
			
			Map<String, InputStreamResource> attach = new HashMap<String, InputStreamResource>();
			attach.put(fileName + ".pdf", new InputStreamResource(pdf));
			attach.put(fileName + ".xml", new InputStreamResource(xml));
			
			emailService.sendMailWithAttach("Correo de prueba", "<h2>Hola</h2>",
					"Email de prueba", attach, para);
		
		} catch (MessagingException ex) {
			logger.error("Error al enviar el email", ex.getMessage());
			throw new PortalException("Error al enviar el email", ex);
		}
	}

}
