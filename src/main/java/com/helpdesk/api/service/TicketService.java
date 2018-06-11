package com.helpdesk.api.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.helpdesk.api.entity.ChangeStatus;
import com.helpdesk.api.entity.Ticket;

@Component
public interface TicketService {

	Ticket createOrUpdate(Ticket ticket);
	
	Ticket findById(String id);
	
	//deleta ticket
	void delete(String id);
	
	//lista todos os tickets
	Page<Ticket> listTicket(int page, int count);
	
	//Salvar as atualizações efetuadas nos tickets
	ChangeStatus createChangeStatus(ChangeStatus change);
	
	//listar
	Iterable<ChangeStatus> listChangeStatus(String ticketId);
	
	//para pesquisar os ticket apenas do usuario sem visão para os demais
	Page<Ticket> findByCurrentUser(int page, int count, String userId);
	
	//Filtro por parametros
	Page<Ticket> findByParameters(int page, int count, String title, String status,String priority);
	
	Page<Ticket> findByParametersAndCurrentUser(int page, int count, String title, String status,String priority,String userId);
	
	//busca pelo numero do ticket
	Page<Ticket> findByNumber(int page, int count, Integer number);
	
	//Fazer resumo de todos os tickets
	Iterable<Ticket> findAll();
	
	public Page<Ticket> findByParametersAndAssignedUser(int page, int count, String title, String status, String priority, String assignedUserId);
}
