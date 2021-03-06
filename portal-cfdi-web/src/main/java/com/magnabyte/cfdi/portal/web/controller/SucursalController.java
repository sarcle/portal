package com.magnabyte.cfdi.portal.web.controller;

import javax.validation.Valid;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.gson.JsonObject;
import com.magnabyte.cfdi.portal.model.cfdi.v32.Comprobante;
import com.magnabyte.cfdi.portal.model.cliente.Cliente;
import com.magnabyte.cfdi.portal.model.cliente.factory.ClienteFactory;
import com.magnabyte.cfdi.portal.model.commons.Usuario;
import com.magnabyte.cfdi.portal.model.documento.Documento;
import com.magnabyte.cfdi.portal.model.documento.DocumentoSucursal;
import com.magnabyte.cfdi.portal.model.documento.TipoDocumento;
import com.magnabyte.cfdi.portal.model.establecimiento.Establecimiento;
import com.magnabyte.cfdi.portal.model.establecimiento.factory.EstablecimientoFactory;
import com.magnabyte.cfdi.portal.model.exception.PortalException;
import com.magnabyte.cfdi.portal.model.ticket.ListaTickets;
import com.magnabyte.cfdi.portal.model.ticket.Ticket;
import com.magnabyte.cfdi.portal.model.utils.FechasUtils;
import com.magnabyte.cfdi.portal.model.utils.StringUtils;
import com.magnabyte.cfdi.portal.service.cfdi.v32.CfdiV32Service;
import com.magnabyte.cfdi.portal.service.cliente.ClienteService;
import com.magnabyte.cfdi.portal.service.documento.ComprobanteService;
import com.magnabyte.cfdi.portal.service.documento.TicketService;
import com.magnabyte.cfdi.portal.service.establecimiento.AutorizacionCierreService;
import com.magnabyte.cfdi.portal.service.establecimiento.EstablecimientoService;
import com.magnabyte.cfdi.portal.service.samba.SambaService;
import com.magnabyte.cfdi.portal.web.cfdi.CfdiService;

/**
 * 
 * @author Magnabyte, S.A. de C.V
 * magnabyte.com.mx
 * Fecha:27/01/2014
 * Clase que represente el controlador de sucursal
 */
@Controller
@SessionAttributes({"establecimiento", "ticket", "cliente", "documento"})
public class SucursalController {

	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private SambaService sambaService;
	
	@Autowired
	private CfdiV32Service cfdiV32Service;
	
	@Autowired
	private ComprobanteService comprobanteService;
	
	@Autowired
	private CfdiService cfdiService;
	
	@Autowired
	private AutorizacionCierreService autCierreService;
	
	@Autowired
	private EstablecimientoService establecimientoService;
	
	private static final Logger logger = LoggerFactory.getLogger(SucursalController.class);
	
	private static String buscaTicketPage = "sucursal/buscaTicket"; 
	
	@RequestMapping("/buscaTicket")
	public String buscaTicket(ModelMap model) {
		model.put("ticket", new Ticket());
		return buscaTicketPage;
	}
	
	@RequestMapping(value = "/validaTicket", method = RequestMethod.POST)
	public String validaTicket(@Valid @ModelAttribute Ticket ticket, BindingResult resultTicket, 
			@ModelAttribute Establecimiento establecimiento, ModelMap model) {
		logger.debug("controller--{}", ticket);
		if (resultTicket.hasErrors()) {
			return buscaTicketPage;
		}
		//FIXME Descomentar para produccion
//		ticketService.validarFechaFacturacion(ticket);
		if (ticketService.ticketExists(ticket, establecimiento)) {
			if (!ticketService.isTicketFacturado(ticket, establecimiento)) {
				model.put("ticket", ticket);
				return "redirect:/buscaRfc";
			} else {
				model.put("ticketProcessed", true);
			}
		} else {
			model.put("invalidTicket", true);
		}
		return buscaTicketPage;
	}
	
	@RequestMapping("/buscaRfc")
	public String buscaRfc(ModelMap model) {
		logger.debug("buscaRfc page");
		logger.debug("Ticket: ---{}", (Ticket)model.get("ticket"));
		model.put("cliente", new Cliente());
		model.put("emptyList", true);
		return "sucursal/buscaRfc";
	}
	
	@RequestMapping("/confirmarDatos/{id}")
	public String confirmarDatos(@PathVariable Integer id, ModelMap model) {
		logger.debug("confirmarDatos page");
		model.put("cliente", clienteService.read(ClienteFactory.newInstance(id)));
		return "sucursal/confirmarDatos";
	}
	
	@RequestMapping("/datosFacturacion/{idDomicilio}")
	public String datosFacturacion(@ModelAttribute Establecimiento establecimiento, @ModelAttribute Cliente cliente, 
			@ModelAttribute Ticket ticket, @PathVariable Integer idDomicilio, ModelMap model) {
		Comprobante comprobante = comprobanteService
				.obtenerComprobantePor(cliente, ticket, idDomicilio, establecimiento, TipoDocumento.FACTURA);
		DocumentoSucursal documento = new DocumentoSucursal();
		documento.setCliente(cliente);
		documento.setId_domicilio(idDomicilio);
		documento.setTicket(ticket);
		documento.setComprobante(comprobante);
		documento.setEstablecimiento(establecimiento);
		documento.setTipoDocumento(TipoDocumento.FACTURA);
		model.put("documento", documento);
		model.put("ticket", ticket);
		return "redirect:/confirmarDatosFacturacion";
	}
	
	@RequestMapping("/confirmarDatosFacturacion")
	public String confirmarDatosFacturacion(@ModelAttribute Documento documento, ModelMap model) {
		cfdiV32Service.isValidComprobanteXml(documento.getComprobante());
		return "sucursal/facturaValidate";
	}
	
	@RequestMapping(value="/fechaCierre", method = RequestMethod.POST)
	public @ResponseBody String fechaCierre(@ModelAttribute Establecimiento establecimiento) {

		String fechaSiguienteCierre = establecimientoService.readFechaCierreById(establecimiento); 
		
		JsonObject json = new JsonObject();
		json.addProperty("fecha", fechaSiguienteCierre);		
		
		return json.toString();
	}
	
	//FIXME Revisar logica
	@RequestMapping(value="/cierre", method = RequestMethod.POST)
	public String cierre(@RequestParam String fechaCierre, @ModelAttribute Usuario usuario,
			@ModelAttribute Establecimiento establecimiento, ModelMap model) {		
		
		usuario.setEstablecimiento(establecimiento);
		
		DateTime today = new DateTime();
		DateTime nowHere = DateTime.now();			
		
		long closeDate = FechasUtils.parseStringToDate(fechaCierre,
				FechasUtils.formatddMMyyyyHyphen).getTime();
		//FIXME Logica temporal
		ListaTickets listaTickets = new ListaTickets();
		listaTickets.setFechaCierre(fechaCierre);
		try {
			if(nowHere.getHourOfDay() > 9) {
				//FIXME revisar hora cierre
//				if(today.isEqual(closeDate)) {
					autCierreService.autorizar(usuario);
//					cfdiService.recuperaTicketsRest(establecimiento, fechaCierre);
					cfdiService.closeOfDay(EstablecimientoFactory
							.newInstanceClave(StringUtils.formatTicketClaveSucursal(establecimiento.getClave())), listaTickets);
//				} else if(today.isBefore(closeDate)) {
//					model.put("error", true);
//					model.put("messageError", "Ya se ha realizado el cierre del día.");
//					return "menu/menu";
//				}
			} else {
				model.put("error", true);
				model.put("messageError", messageSource.getMessage("cierre.error.hora", null, null));
				return "menu/menu";
			}
			
		} catch (PortalException ex) {
			model.put("error", true);
			model.put("messageError", ex.getMessage());
			return "menu/menu";
		}
		return "redirect:/successCierre";
	}
	
	@RequestMapping("/successCierre")
	public String successCierre(ModelMap model) {
		model.put("success", true);
		model.put("messageSuccess", messageSource.getMessage("cierre.success", null, null));
		return "menu/menu";
	}
}
