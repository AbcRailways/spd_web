package kz.railways.models;

import java.io.IOException;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;

import org.primefaces.context.RequestContext;

import models.PdfMB;
import models.UserMB;


@ManagedBean(name = "mainMB")
@ViewScoped
public class MainMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	
	@ManagedProperty("#{userMB}")
    private UserMB userService;		
	
	private String shortText = "";
	private int sign;
	
	@PostConstruct
	public void init() {
		
	}

	public void signDocs(int sign) {//подписание выбранного документа
		try {
			 if (userService.getStreamedContent().getStream() != null) {// проверяем открыт ли inputstream, если да то закрываем
				 userService.getStreamedContent().getStream().close();
				 
			 }
			 if (sign == 1) {
				 PdfMB.initkey(userService.getSelectedDoc().getLinkDoc(), "C://SPD_WEB//DSAPP.pfx", "qwe".toCharArray(), this.userService.getUser().getSignAlias(),  this.userService.getUser().getPassKey().toCharArray());
			 	FacesContext.getCurrentInstance().addMessage(null, 
			 			new FacesMessage(FacesMessage.SEVERITY_INFO, "Документ успешно подписан!",  ""));
			 }
			 signDocDataBase(sign);
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка подписания документа!",  ""));
			e.printStackTrace();
		}
	}
	
	public void check(int sign) {		
		if (sign == 2) soglosovanie(sign);
		else if (sign == 3) RequestContext.getCurrentInstance().execute("PF('dlg4').show()");
		
		if (sign == 1) signDocs(sign);
		else if (sign == 4) RequestContext.getCurrentInstance().execute("PF('dlg4').show()"); 
		this.sign = sign;
	}
	
	public void otkaz() throws IOException {
		if (this.shortText.equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите причину отказа!",  ""));
		} else {
			
			if (this.sign == 3) soglosovanie(this.sign);
			else signDocs(this.sign);
			
			RequestContext.getCurrentInstance().execute("PF('dlg4').hide()");
			RequestContext.getCurrentInstance().update("rightForm:pnlForm");
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
		}
	}
	
	//отметка в бд о согласовании
	public void soglosovanie(int sogl) {
		Connection conn = null;
		String word = "";
		
		if (sogl == 2) word = "Документ согласован!";
		else word = "Отказано в согласовании документа!";
		
		
    	try {
    		conn = dataSource.getConnection();
    		
    		try (PreparedStatement ps = conn.prepareCall("{call MAS.SOGLASOVANIE(?,?,?,?,?)}")) {
    			
    			ps.setInt(1, userService.getSelectedIncoming().getUserIdOtpr());
    			ps.setInt(2, userService.getSelectedIncoming().getUserIdPol());
    			ps.setString(3, userService.getSelectedIncoming().getDocId());
    			ps.setString(4, this.shortText);
    			ps.setInt(5, sogl);
    			
    			ps.execute();
    		}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД!", e.getMessage()));
			throw (EJBException) new EJBException(e).initCause(e); 
		}
		finally {
			try {
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_INFO, word,  ""));
				conn.close();				
			} catch (SQLException fse) {
				fse.printStackTrace();
			}
		}
	}	
	
	public UserMB getUserService() {
		return userService;
	}

	public void setUserService(UserMB userService) {
		this.userService = userService;
	}

  //отметка в бд о подписании документа
    public void signDocDataBase(int sign) {
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection();
    		
    		try (PreparedStatement ps = conn.prepareCall("{call MAS.PODPISANIE(?,?,?,?,?)}")) {
    			
    			ps.setInt(1, userService.getSelectedIncoming().getUserIdOtpr());
    			ps.setInt(2, userService.getSelectedIncoming().getUserIdPol());
    			ps.setString(3, userService.getSelectedIncoming().getDocId());
    			ps.setString(4, this.shortText);
    			ps.setInt(5, sign);
    			
    			ps.execute();
    		}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД!", e.getMessage()));
			throw (EJBException) new EJBException(e).initCause(e); 
		}
		finally {
			try {
				conn.close();				
			} catch (SQLException fse) {
				fse.printStackTrace();
			}
		}
    }

	public String getShortText() {
		return shortText;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public int getSign() {
		return sign;
	}

	public void setSign(int sign) {
		this.sign = sign;
	}
 
}
