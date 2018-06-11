package com.helpdesk.api.dto;

import java.io.Serializable;

public class Summary implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer amountNovo;
	private Integer amountResolvido;
	private Integer amountAprovado;
	private Integer amountDesaprovado;
	private Integer amountDesignado;
	private Integer amountFechado;
	private Integer amountEm_Andamento;
	
	public Integer getAmountNovo() {
		return amountNovo;
	}
	public void setAmountNovo(Integer amountNovo) {
		this.amountNovo = amountNovo;
	}
	public Integer getAmountResolvido() {
		return amountResolvido;
	}
	public void setAmountResolvido(Integer amountResolvido) {
		this.amountResolvido = amountResolvido;
	}
	public Integer getAmountAprovado() {
		return amountAprovado;
	}
	public void setAmountAprovado(Integer amountAprovado) {
		this.amountAprovado = amountAprovado;
	}
	public Integer getAmountDesaprovado() {
		return amountDesaprovado;
	}
	public void setAmountDesaprovado(Integer amountDesaprovado) {
		this.amountDesaprovado = amountDesaprovado;
	}
	public Integer getAmountDesignado() {
		return amountDesignado;
	}
	public void setAmountDesignado(Integer amountDesignado) {
		this.amountDesignado = amountDesignado;
	}
	public Integer getAmountFechado() {
		return amountFechado;
	}
	public void setAmountFechado(Integer amountFechado) {
		this.amountFechado = amountFechado;
	}
	public Integer getAmountEm_Andamento() {
		return amountEm_Andamento;
	}
	public void setAmountEm_Andamento(Integer amountEm_Andamento) {
		this.amountEm_Andamento = amountEm_Andamento;
	}
	
	


}
