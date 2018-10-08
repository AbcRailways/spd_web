package kz.railways.models;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import models.UserMB;

@ManagedBean(name = "otkazMB")
@ViewScoped
public class OtkazMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManagedProperty("#{userMB}")
    private UserMB userService;	
	
	@PostConstruct
    public void init() {
		
	}

	public UserMB getUserService() {
		return userService;
	}

	public void setUserService(UserMB userService) {
		this.userService = userService;
	}
	
}
