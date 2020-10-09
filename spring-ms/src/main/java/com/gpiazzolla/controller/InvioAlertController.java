package com.gpiazzolla.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invio-alert")
public class InvioAlertController {


	protected Logger logger = LoggerFactory.getLogger(InvioAlertController.class);

	@Autowired
	private SimpMessagingTemplate template;
	
	@GetMapping(value="/test", consumes ="application/json")
	public void test(@RequestParam("codiceFiscale") String codiceFiscale) {
		
		template.convertAndSend("/topic/" + codiceFiscale, "Inviato dal Backend");
		
	}
}
