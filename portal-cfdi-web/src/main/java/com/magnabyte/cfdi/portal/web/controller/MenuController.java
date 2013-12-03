package com.magnabyte.cfdi.portal.web.controller;

import java.io.File;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.magnabyte.cfdi.portal.model.Sucursal;

@Controller
@SessionAttributes("sucursal")
public class MenuController {
	
//	@Autowired
//	private NewWebService newWebService;
	
	private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

	@RequestMapping("/menu")
	public String menu(ModelMap model, Principal principal) {
		logger.debug("menu sucursal");
		Sucursal sucursal = new Sucursal();
		sucursal.setNombre(principal.getName());
		model.put("sucursal", sucursal);
		
		//Web Service
//		logger.debug(newWebService.suma(69, 2));
//		logger.debug(newWebService.operation("Omar"));
		
		if (sucursal.getNombre().equalsIgnoreCase("magna")) {
			return "menu/menu";
		} else {
			return "redirect:/menuCorp";
		}
	}
	
	@RequestMapping("/menuCorp")
	public String menuCorp(ModelMap model) {
		logger.debug("menuCorp...");
		List<Sucursal> list = new ArrayList<Sucursal>();
		for(int i = 0; i < 10; i++) {
			list.add(new Sucursal(i + 1, "Nombre " + i, "Password " + i));
		}
		model.put("lista", list);
		return "menu/menuCorp";
	}
	
	@RequestMapping("/cfdi/menu")
	public String menuCliente(ModelMap model) {
		logger.debug("menu cliente");
		return "menu/menu";
	}
	
	@RequestMapping("/about")
	public String about(ModelMap model, HttpSession session) {
		logger.debug("about");
		logger.debug("suc--{}", (Sucursal)session.getAttribute("sucursal"));
		logger.debug("sessionId={}", session.getId());
		return "menu/about";
	}
	
	@RequestMapping("/cfdi/list")
	public String list() {
		logger.debug("list...");
		File dir = new File("/home");
		if (dir.exists()) {
			logger.debug("el directorio existe");
			for (String name : dir.list()) {
				logger.debug("-" + name);
			}
		} else {
			logger.debug("no existe el directorio");
		}
		return "menu/about";
	}
}
