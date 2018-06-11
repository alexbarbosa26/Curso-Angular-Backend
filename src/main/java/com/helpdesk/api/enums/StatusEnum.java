package com.helpdesk.api.enums;

public enum StatusEnum {
	
	Novo,
	Em_Andamento,
	Resolvido,
	Desaprovado,
	Aprovado,
	Fechado,
	Designado;

	public static StatusEnum getStatus(String status) {
		switch(status) {
		
		case "Novo": return Novo;
		case "Em_Andamento": return Em_Andamento;
		case "Resolvido": return Resolvido;
		case "Desaprovado": return Desaprovado;
		case "Aprovado": return Aprovado;
		case "Fechado": return Fechado;
		case "Designado": return Designado;
		default : return Novo;
		
		}
		
	}
}
