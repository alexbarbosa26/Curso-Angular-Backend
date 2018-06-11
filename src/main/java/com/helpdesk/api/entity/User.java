package com.helpdesk.api.entity;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.helpdesk.api.enums.ProfileEnum;

@Document
public class User {
	
	@Id
	private String id;
	
	@NotBlank(message="Nome obrigatório!")
	private String name;
	
	@Indexed(unique=true)
	@NotBlank(message="Email é obrigatório!")
	@Email(message="Email inválido!")
	private String email;
	
	@NotBlank(message="Senha obrigatório!")
	private String password;
	
	private ProfileEnum profile;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ProfileEnum getProfile() {
		return profile;
	}

	public void setProfile(ProfileEnum profile) {
		this.profile = profile;
	}
	
	

}
