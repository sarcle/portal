package com.magnabyte.cfdi.portal.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import mx.gob.sat.cfd._3.Comprobante;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
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
import com.magnabyte.cfdi.portal.model.cliente.Cliente;
import com.magnabyte.cfdi.portal.model.cliente.factory.ClienteFactory;
import com.magnabyte.cfdi.portal.model.commons.Usuario;
import com.magnabyte.cfdi.portal.model.documento.DocumentoSucursal;
import com.magnabyte.cfdi.portal.model.documento.TipoDocumento;
import com.magnabyte.cfdi.portal.model.establecimiento.Establecimiento;
import com.magnabyte.cfdi.portal.model.exception.PortalException;
import com.magnabyte.cfdi.portal.model.ticket.Ticket;
import com.magnabyte.cfdi.portal.service.cliente.ClienteService;
import com.magnabyte.cfdi.portal.service.documento.DocumentoService;
import com.magnabyte.cfdi.portal.service.documento.TicketService;
import com.magnabyte.cfdi.portal.service.establecimiento.AutorizacionCierreService;
import com.magnabyte.cfdi.portal.service.establecimiento.EstablecimientoService;
import com.magnabyte.cfdi.portal.service.samba.SambaService;
import com.magnabyte.cfdi.portal.web.cfdi.CfdiService;

@Controller
@SessionAttributes({"establecimiento", "ticket", "cliente", "documento"})
public class SucursalController {

	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private TicketService ticketService;
	
	@Autowired
	private SambaService sambaService;
	
	@Autowired
	private DocumentoService documentoService;
	
	@Autowired
	private CfdiService cfdiService;
	
	@Autowired
	private TaskExecutor executor;
	
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
		if (ticketService.ticketExists(ticket, establecimiento)) {
			if (!ticketService.ticketProcesado(ticket, establecimiento)) {
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
		Comprobante comprobante = documentoService.obtenerComprobantePor(cliente, ticket, idDomicilio, establecimiento);
		DocumentoSucursal documento = new DocumentoSucursal();
		documento.setCliente(cliente);
		documento.setTicket(ticket);
		documento.setComprobante(comprobante);
		documento.setEstablecimiento(establecimiento);
		documento.setTipoDocumento(TipoDocumento.FACTURA);
		model.put("documento", documento);
		model.put("comprobante", comprobante);
		model.put("ticket", ticket);
		return "sucursal/facturaValidate";
	}
	
	@RequestMapping(value="/fechaCierre", method = RequestMethod.POST)
	public @ResponseBody String fechaCierre(@ModelAttribute Establecimiento establecimiento) {
		return establecimientoService.readFechaCierreById(establecimiento);
	}
	
	@RequestMapping(value="/cierre", method = RequestMethod.POST)
	public @ResponseBody String cierre(@RequestParam String usuario, @RequestParam String password,
			@ModelAttribute Establecimiento establecimiento, ModelMap model, HttpServletRequest request) {
		Usuario user = new Usuario();
		
		user.setEstablecimiento(establecimiento);
		user.setUsuario(usuario);
		user.setPassword(password);
		
		logger.debug("Llegue a cierre");
		try {
			autCierreService.autorizar(user);
		} catch (PortalException ex) {
			JsonObject json = new JsonObject();
			json.addProperty("error", ex.getMessage());
//			model.put("errorForm", ex.getMessage());
			return json.toString();
		}
		
//		logger.debug("cierre...");
//		cfdiService.closeOfDay(establecimiento, request);
		return "menu/menu";
	}
}
