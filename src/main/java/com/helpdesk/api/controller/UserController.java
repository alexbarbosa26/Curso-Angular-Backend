package com.helpdesk.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.helpdesk.api.service.UserService;
import com.helpdesk.api.entity.User;
import com.helpdesk.api.response.Response;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Metodo para criar usuario
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> create(HttpServletRequest resquest, @RequestBody User user,
			BindingResult result) {

		Response<User> response = new Response<User>();

		try {
			validateCreateUser(user, result);

			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User userPersisted = (User) userService.createOrUpdate(user);

			response.setData(userPersisted);

		} catch (DuplicateKeyException e) {
			response.getErros().add("Email já está registrado!");
			return ResponseEntity.badRequest().body(response);

		} catch (Exception e2) {
			response.getErros().add(e2.getMessage());
			return ResponseEntity.badRequest().body(response);

		}
		return ResponseEntity.ok(response);

	}

	private void validateCreateUser(User user, BindingResult result) {
		if (user.getEmail() == null) {
			result.addError(new ObjectError("User", "Email não informado"));
		}
	}

	// Metodo para atualizar usuarios

	@PutMapping
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> update(HttpServletRequest resquest, @RequestBody User user,
			BindingResult result) {

		Response<User> response = new Response<User>();

		try {
			validateUpdateUser(user, result);

			if (result.hasErrors()) {
				result.getAllErrors().forEach(error -> response.getErros().add(error.getDefaultMessage()));
				return ResponseEntity.badRequest().body(response);
			}

			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User userPersisted = (User) userService.createOrUpdate(user);

			response.setData(userPersisted);

		} catch (DuplicateKeyException e) {
			response.getErros().add(e.getMessage());
			return ResponseEntity.badRequest().body(response);

		}
		return ResponseEntity.ok(response);

	}

	private void validateUpdateUser(User user, BindingResult result) {
		if (user.getId() == null) {
			result.addError(new ObjectError("User", "Codigo não informado"));
		}
		if (user.getEmail() == null) {
			result.addError(new ObjectError("User", "Email não informado"));
		}
	}

	// Metodo para consultar pelo Id
	@GetMapping(value = "{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<User>> findById(@PathVariable("id") String id) {

		Response<User> response = new Response<User>();

		User user = userService.findById(id);

		if (user == null) {
			response.getErros().add("Registro do Código não encontrado: " + id);
			return ResponseEntity.badRequest().body(response);

		}

		response.setData(user);
		return ResponseEntity.ok(response);

	}
	
	
	//Metodo para deletar Usuario
	@DeleteMapping(value="{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<String>> delete(@PathVariable("id") String id){
		Response<String> response = new Response<String>();		
		User user = userService.findById(id);
		
		if (user == null) {
			response.getErros().add("Registro do Código não encontrado: " + id);
			return ResponseEntity.badRequest().body(response);

		}

		userService.delete(id);
		
		return ResponseEntity.ok(new Response<String>());
	}
	
	//Metodo para retornar todos os usuarios
	@GetMapping(value="{page}/{count}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Response<Page<User>>> findAll(@PathVariable int page,@PathVariable  int count){
	
		Response<Page<User>> response = new Response<Page<User>>();
		Page<User> users=userService.findAll(page, count);
		
		response.setData(users);		
		
		return ResponseEntity.ok(response);
	}
	
	
}