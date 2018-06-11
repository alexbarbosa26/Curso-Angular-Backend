package com.helpdesk.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.api.dto.Summary;
import com.helpdesk.api.entity.ChangeStatus;
import com.helpdesk.api.entity.Ticket;
import com.helpdesk.api.entity.User;
import com.helpdesk.api.enums.ProfileEnum;
import com.helpdesk.api.enums.StatusEnum;
import com.helpdesk.api.response.Response;
import com.helpdesk.api.security.jwt.JwtTokenUtil;
import com.helpdesk.api.service.TicketService;
import com.helpdesk.api.service.UserService;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketController {

	@Autowired
	private TicketService ticketService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private UserService userService;

	//*********************************************************************************************
	// Metodo para criar ticket
	@PostMapping()
	@PreAuthorize("hasAnyRole('USERS','ADMIN')")
	public ResponseEntity<Response<Ticket>> createOrUpdate(HttpServletRequest request, @RequestBody Ticket ticket,
			BindingResult result) {

		Response<Ticket> response = new Response<>();
		try {
			validateCreateTicket(ticket, result);

			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			ticket.setStatus(StatusEnum.getStatus("Novo"));
			ticket.setUser(userFromRequest(request));
			ticket.setDate(new Date());
			ticket.setNumber(generateNumber());
			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticket);

			response.setData(ticketPersisted);

		} catch (Exception e) {
			response.getErros().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}

		return ResponseEntity.ok(response);
	}

	private void validateCreateTicket(Ticket ticket, BindingResult result) {
		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Titulo não informado"));
			return;
		}
	}

	public User userFromRequest(HttpServletRequest request) {

		String token = request.getHeader("Authorization");
		String nome = jwtTokenUtil.getUsernameFromToken(token);

		return userService.findByEmail(nome);
	}

	private Integer generateNumber() {

		Random random = new Random();

		return random.nextInt(99999999);

	}

	// **************************************************************************
	//Metodo para gerar um log das modificações efetuadas no ticket
	@PutMapping()
	@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
	public ResponseEntity<Response<Ticket>> update(HttpServletRequest request, @RequestBody Ticket ticket,
			BindingResult result) {

		Response<Ticket> response = new Response<>();

		try {
			validateUpdateTicket(ticket, result);

			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			Ticket ticketCurrent = ticketService.findById(ticket.getId());
			ticket.setStatus(ticketCurrent.getStatus());
			ticket.setUser(ticketCurrent.getUser());
			ticket.setDate(ticketCurrent.getDate());
			ticket.setDescription(ticketCurrent.getDescription());
			ticket.setNumber(ticketCurrent.getNumber());

			if (ticketCurrent.getAssignedUser() != null) {
				ticketCurrent.setAssignedUser(ticketCurrent.getAssignedUser());
			}

			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticket);

			response.setData(ticketPersisted);

		} catch (Exception e) {

			response.getErros().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);

		}
		return ResponseEntity.ok(response);
	}

	private void validateUpdateTicket(Ticket ticket, BindingResult result) {
		if (ticket.getId() == null) {
			result.addError(new ObjectError("Ticket", "Código não informado"));
			return;
		}

		if (ticket.getTitle() == null) {
			result.addError(new ObjectError("Ticket", "Titulo não informado"));
			return;
		}
	}

	// **************************************************************************
	//Metodo para buscar o ticket pelo ID/codigo
	@GetMapping(value = "{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN','ADMIN')")
	public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {

		Response<Ticket> response = new Response<>();

		Ticket ticket = ticketService.findById(id);
		if (ticket == null) {
			response.getErros().add("Registro não encontrado: " + id);
			return ResponseEntity.badRequest().body(response);
		}

		List<ChangeStatus> changes = new ArrayList<>();

		Iterable<ChangeStatus> changesCurrent = ticketService.listChangeStatus(ticket.getId());

		for (Iterator<ChangeStatus> iterator = changesCurrent.iterator(); iterator.hasNext();) {

			ChangeStatus changeStatus = (ChangeStatus) iterator.next();

			changeStatus.setTicket(null);
			changes.add(changeStatus);
		}
		ticket.setChanges(changes);
		response.setData(ticket);
		return ResponseEntity.ok(response);
	}

	// ********************************************************************************************
	//Metodo para deltar o ticket
	@DeleteMapping(value = "{id}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN','ADMIN')")
	public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {

		Response<String> response = new Response<String>();
		Ticket ticket = ticketService.findById(id);

		if (ticket == null) {
			response.getErros().add("Registro não encontrado: " + id);
			return ResponseEntity.badRequest().body(response);
		}

		ticketService.delete(id);
		return ResponseEntity.ok(new Response<String>());

	}
	// ********************************************************************************************
	//Metodo para listar todos os tickets de forma paginada
	@GetMapping(value = "{page}/{count}")
	@PreAuthorize("hasAnyRole('USERS','TECHNICIAN','ADMIN')")
	public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, @PathVariable("page") int page,
			@PathVariable("count") int count) {

		Response<Page<Ticket>> response = new Response<Page<Ticket>>();

		Page<Ticket> tickets = null;

		User userRequest = userFromRequest(request);

		if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
			tickets = ticketService.listTicket(page, count);

		}else if (userRequest.getProfile().equals(ProfileEnum.ROLE_ADMIN)) {
			tickets = ticketService.listTicket(page, count);

		}else if (userRequest.getProfile().equals(ProfileEnum.ROLE_USERS)) {
			tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
		}
		response.setData(tickets);
		return ResponseEntity.ok(response);

	}

	// *******************************************************************************************
	//Metodo para efetuar buscar por diversos parametros
	@GetMapping(value = "{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN','ADMIN')")
	public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request,
			@PathVariable("page") int page,
			@PathVariable("count") int count,
			@PathVariable("number") Integer number,
			@PathVariable("title") String title,
			@PathVariable("status") String status,
			@PathVariable("priority") String priority,
			@PathVariable("assigned") boolean assigned) {

		title = title.equals("uninformed") ? "" : title;
		status = status.equals("uninformed") ? "" : status;
		priority = priority.equals("uninformed") ? "" : priority;

		Response<Page<Ticket>> response = new Response<Page<Ticket>>();
		Page<Ticket> tickets = null;
		if (number > 0) {
			tickets = ticketService.findByNumber(page, count, number);
		} else {
			User userRequest = userFromRequest(request);
			if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
				if (assigned) {
					tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority,userRequest.getId());

				} else {
					tickets = ticketService.findByParameters(page, count, title, status, priority);
				}
			
			}else if (userRequest.getProfile().equals(ProfileEnum.ROLE_USERS)) {
				
				tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, priority, userRequest.getId());
			}
		}

		response.setData(tickets);
		return ResponseEntity.ok(response);
	}
	
	@PutMapping(value = "/{id}/{status}")
	@PreAuthorize("hasAnyRole('USERS','TECHNICIAN','ADMIN')")
	public ResponseEntity<Response<Ticket>> changeStatus(
			@PathVariable("id") String id,
			@PathVariable("status") String status,
			HttpServletRequest request,
			@RequestBody Ticket ticket,
			BindingResult result) {
		
		Response<Ticket> response = new Response<Ticket>();
		try {
			validateChangeStatus(id, status, result);
			
			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}
			
			Ticket ticketCurrent = ticketService.findById(id);
			ticketCurrent.setStatus(StatusEnum.getStatus(status));
			if(status.equals("Designado")) {
				ticketCurrent.setAssignedUser(userFromRequest(request));
			}
			Ticket ticketPersisted = (Ticket) ticketService.createOrUpdate(ticketCurrent);
			ChangeStatus changeStatus = new ChangeStatus();
			changeStatus.setUserChange(userFromRequest(request));
			changeStatus.setDateChangeStatus(new Date());
			changeStatus.setStatus(StatusEnum.getStatus(status));
			changeStatus.setTicket(ticketPersisted);
			ticketService.createChangeStatus(changeStatus);
			response.setData(ticketPersisted);
		} catch (Exception e) {
			response.getErros().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);
		}
		return ResponseEntity.ok(response);
	}
	
	private void validateChangeStatus(String id,String status, BindingResult result) {
		if (id == null || id.equals("")) {
			result.addError(new ObjectError("Ticket", "Id no information"));
			return;
		}
		if (status == null || status.equals("")) {
			result.addError(new ObjectError("Ticket", "Status no information"));
			return;
		}
	}
	
	@GetMapping(value = "/summary")
	public ResponseEntity<Response<Summary>> findChart() {
		Response<Summary> response = new Response<Summary>();
		Summary chart = new Summary();
		int amountNovo = 0;
		int amountResolvido = 0;
		int amountAprovado = 0;
		int amountDesaprovado = 0;
		int amountDesignado = 0;
		int amountFechado = 0;
		int amountEm_Andamento=0;
		Iterable<Ticket> tickets = ticketService.findAll();
		if (tickets != null) {
			for (Iterator<Ticket> iterator = tickets.iterator(); iterator.hasNext();) {
				Ticket ticket = iterator.next();
				if(ticket.getStatus().equals(StatusEnum.Novo)){
					amountNovo ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Resolvido)){
					amountResolvido ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Aprovado)){
					amountAprovado ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Desaprovado)){
					amountDesaprovado ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Designado)){
					amountDesignado ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Fechado)){
					amountFechado ++;
				}
				if(ticket.getStatus().equals(StatusEnum.Em_Andamento)){
					amountEm_Andamento ++;
				}
			}	
		}
		chart.setAmountNovo(amountNovo);
		chart.setAmountResolvido(amountResolvido);
		chart.setAmountAprovado(amountAprovado);
		chart.setAmountDesaprovado(amountDesaprovado);
		chart.setAmountDesignado(amountDesignado);
		chart.setAmountFechado(amountFechado);
		chart.setAmountEm_Andamento(amountEm_Andamento);
		response.setData(chart);
		return ResponseEntity.ok(response);
	}
}
