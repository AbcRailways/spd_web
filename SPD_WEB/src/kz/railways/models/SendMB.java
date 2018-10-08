package kz.railways.models;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.primefaces.event.SelectEvent;

import kz.railways.entities.LinkDoc;
import kz.railways.entities.TypeOrThemeDoc;
import kz.railways.entities.User;
import models.UserMB;
import views.HelperUtils;

@ManagedBean(name = "sendMB")
@ViewScoped
public class SendMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "jdbc/DB2Dsapp")
	private DataSource dataSource;

	
	@ManagedProperty("#{userMB}")
	private UserMB userService;

	@ManagedProperty("#{newDocMB}")
	private NewDocMB newDocMB;
	
	private Date dateIspoln;
	
	private String searchOneRadio = "name";
	private String searchInputTxt = "";
	private String fullName = "";
	private String position = "";
	
	
	private int tipOtprDoc;
	private int themeId;
	private int typeId;
	
	public List<User> listAllUsers;//лист всех пользователей
	public List<User> listSoglUser;//лист пользователей для согласования
	public List<User> listSearchUser;//лист поиска пользователей
	public List<User> listPoluch;//лист получателей
	public List<User> listViewUserSogl;
	public List<User> listViewUserPoluch;
	
	public List<TypeOrThemeDoc> listThemeDoc;
	public List<TypeOrThemeDoc> listTypeDoc;
	
	private User selectedSearchUser;// найденый пользователь
	private User selectedUsr;// для перемещения позиции пользователя
	private List<User> signUsr;// пользователь для подписания
	private User removeUser;
	
	private LinkDoc selectedDoc;//выбранный документ
	
	private boolean usrSign = false;// если true то адресная книга работает для поиска поьзователя для подписания
	private boolean poluchateli = false;// если true то адресная книга работает для поиска получателей
	
	@PostConstruct
	public void init() {
		this.dateIspoln = new Date(System.currentTimeMillis());
		System.out.println("send init---------------------------");
		if (this.listAllUsers == null) {
			this.listAllUsers = listUserEJB();
		}
		if (this.listThemeDoc == null) {
			this.listThemeDoc = listTypeOrThemeDoc(0);
		}
		if (this.listTypeDoc == null) {
			this.listTypeDoc = listTypeOrThemeDoc(1);
		}
		this.listSoglUser = new ArrayList<User>();
		this.listSearchUser = new ArrayList<User>();
		this.listPoluch = new ArrayList<User>();
		this.signUsr = new ArrayList<User>();
		setSelectedDoc(userService.getSelectedDoc());
	}
	
	public void addUsrSogl() {// открытие формы согласование для поиска пользователей
		this.usrSign = false;
		this.poluchateli = false;
		this.searchInputTxt = "";
		this.listSearchUser = new ArrayList<User>();
		RequestContext.getCurrentInstance().update("sendForm:dlgForm:searchPnlGrid");
		RequestContext.getCurrentInstance().execute("PF('dlg1').show()");
	}
	
	public void addUsrSign() {// открытие формы подписание для поиска пользователя
		this.usrSign = true;
		this.poluchateli = false;
		this.searchInputTxt = "";
		this.listSearchUser = new ArrayList<User>();
		RequestContext.getCurrentInstance().update("sendForm:dlgForm:searchPnlGrid");
		RequestContext.getCurrentInstance().execute("PF('dlg1').show()");
	}
	
	public void addUsrPoluch() {// открытие формы для поиска получателей
		this.usrSign = false;
		this.poluchateli = true;
		this.searchInputTxt = "";
		this.listSearchUser = new ArrayList<User>();
		RequestContext.getCurrentInstance().update("sendForm:dlgForm:searchPnlGrid");
		RequestContext.getCurrentInstance().update("sendForm:dlgForm:panelGidUsr");
		RequestContext.getCurrentInstance().execute("PF('dlg1').show()");
	}
	
	public void exitDlg() {// выход из адресной книги	
		if (this.usrSign == false && this.poluchateli == false)
			if (this.tipOtprDoc == 0 && (this.listSoglUser.size() > 0)) 
	    		FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите тип согласования документа!", ""));
	    			    	
		else {
			this.listViewUserSogl = new ArrayList<User>();
			RequestContext.getCurrentInstance().execute("PF('dlg1').hide()");
			this.listViewUserSogl.addAll(this.listSoglUser);
			RequestContext.getCurrentInstance().update("sendForm:viewList");
		}
		
		if (this.poluchateli == true) {
			this.listViewUserPoluch = new ArrayList<User>();
			RequestContext.getCurrentInstance().execute("PF('dlg1').hide()");
			this.listViewUserPoluch.addAll(this.listPoluch);
			RequestContext.getCurrentInstance().update("sendForm:viewList1");
		}
		
		if (this.usrSign == true) {
			RequestContext.getCurrentInstance().execute("PF('dlg1').hide()");
			RequestContext.getCurrentInstance().update("sendForm:signPnlGrid");
		}
	}
	
	public void viewMessage() {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Пользователь выбран для параллельного согласования!",  ""));
	}
	
	public void removeUser(boolean sogl) {
		if (this.removeUser == null) {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Выберите пользователя для удаления!",  ""));
		} else if (sogl) {
			System.out.println("удаление пользователя из согласования");
			this.listSoglUser.remove(this.removeUser);
			
		} else {
			System.out.println("удаление пользователя из получения");
			this.listPoluch.remove(this.removeUser);
			
		}
		this.listAllUsers = new ArrayList<User>();
		this.listAllUsers = listUserEJB();
		RequestContext.getCurrentInstance().update("sendForm:dlgForm:panelGidUsr");
		this.removeUser = null;
	}
	
	public void onRowSelectUser(SelectEvent event) {
		this.removeUser = ((User) event.getObject());
		System.out.println(this.removeUser.getFullName()+" "+this.removeUser.getSurname());
	}
	
	public void dblClick(final SelectEvent event) {// при двойном клике по найденому пользователю, он перемещается в список для соглосования		
		User usr = (User) event.getObject();
		if (this.usrSign == false && this.poluchateli == false) {
			
			
			if (this.listPoluch.contains(usr)) 
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список получателей!",  ""));					
			 else
				 if (this.signUsr.contains(usr)) 
						FacesContext.getCurrentInstance().addMessage(null, 
								new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список подписантов!",  ""));					
					 else
						 if (this.listSoglUser.contains(usr)) 
							 FacesContext.getCurrentInstance().addMessage(null, 
									 new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен!",  ""));					
						 else 
							 this.listSoglUser.add(usr);
		}
		
		 if (this.poluchateli == true) {
			 if (this.listSoglUser.contains(usr)) 
				 FacesContext.getCurrentInstance().addMessage(null, 
						 new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список согласующих!",  ""));					
			 else
				 if (this.signUsr.contains(usr)) 
						FacesContext.getCurrentInstance().addMessage(null, 
								new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список подписантов!",  ""));					
					 else
						 if (this.listPoluch.contains(usr)) 
							 FacesContext.getCurrentInstance().addMessage(null, 
									 new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен!",  ""));					
						 else 
							 this.listPoluch.add(usr);	
		} 
		 
		 if (this.usrSign == true) {
			
			if (this.listSoglUser.contains(usr)) 
				 FacesContext.getCurrentInstance().addMessage(null, 
						 new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список согласующих!",  ""));					
			 else
				 if (this.listPoluch.contains(usr)) 
						FacesContext.getCurrentInstance().addMessage(null, 
								new FacesMessage(FacesMessage.SEVERITY_FATAL, "Данный пользователь уже добавлен в список получателей!",  ""));					
					 else {
						 this.signUsr = new ArrayList<User>();
						 this.signUsr.add(usr);
						 this.fullName = usr.getFullName() + " " + usr.getSurname() ;
						 this.position = usr.getPosition();
					 }			
		}
		 
		System.out.println(usr.getFullName());
		this.listSearchUser = new ArrayList<User>();
		
		if (this.poluchateli == false)
			RequestContext.getCurrentInstance().update("sendForm:dlgForm:dataTbl");
		else 
			RequestContext.getCurrentInstance().update("sendForm:dlgForm:dataTbl1");
	}
	
	//===========поиск по имени или фамилии=========================
	public void searchUser() {
		this.listSearchUser = new ArrayList<User>();
		if (this.searchInputTxt.equals("")) {
			String msg = "";
			if (this.searchOneRadio.equals("name")) 
				msg = "Имя";
			else msg = "Фамилию";
			
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Введите " + msg + "!",  ""));
		} else {
			if (this.searchOneRadio.equals("name")) 
				searchName();
			else 
				searchSurname();
		}
	}
	
	public void searchName() {
		for (int i = 0; i < this.listAllUsers.size(); i++) {
			int result = this.listAllUsers.get(i).getFullName().compareToIgnoreCase(this.searchInputTxt);
			if (result == 0) 
				this.listSearchUser.add(this.listAllUsers.get(i));			
		}
	}
	public void searchSurname() {
		for (int i = 0; i < this.listAllUsers.size(); i++) {
			int result = this.listAllUsers.get(i).getSurname().compareToIgnoreCase(this.searchInputTxt);
			if (result == 0) 				
				this.listSearchUser.add(this.listAllUsers.get(i));			
				
		}
	}
	//============================================================
	
	public List<TypeOrThemeDoc> listTypeOrThemeDoc(int type) {
		
		String sql = "select * from ";
		if (type == 0) 
			sql = sql + "THEME_DOC order by THEME_ID";
		else sql = sql + "TYPE_DOC order by TYPE_ID";
		
		List<TypeOrThemeDoc> listDoc = new ArrayList<TypeOrThemeDoc>();
		
		try (Connection conn = dataSource.getConnection();		
				PreparedStatement ps = conn.prepareStatement(sql);) {
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				TypeOrThemeDoc ls = new TypeOrThemeDoc();
				if (type == 0) {
					ls.setId(rs.getInt("THEME_ID"));
					ls.setTypeOrThemeDoc(rs.getString("THEME"));
				} else {
					ls.setId(rs.getInt("TYPE_ID"));
					ls.setTypeOrThemeDoc(rs.getString("TYPE"));
				}
				
				listDoc.add(ls);
			}
			rs.close();
		}	catch (SQLException e) {
			e.printStackTrace();
			throw new EJBException(e);
		}
		return listDoc;
	}
	
	//загрузка в список всех пользователей
    public List<User> listUserEJB() {
		
		String sql = "select * from USER_INFO where USER_ID not like ? order by USER_ID";
		
		List<User> listUser = new ArrayList<User>();
		try (Connection conn = dataSource.getConnection();		
				PreparedStatement ps = conn.prepareStatement(sql);) {
				
				ps.setInt(1, userService.getUser().getUserId());
				ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				User user = new User();
				user.setUserId(rs.getInt("USER_ID"));
				user.setSurname(rs.getString("SURNAME"));
				user.setFullName(rs.getString("FULL_NAME"));
				user.setPosition(rs.getString("POSITION_U"));
				user.setDepartment(rs.getString("DEPARTMENT"));
				user.setnPhone(rs.getString("N_PHONE"));
				user.setSignAlias(rs.getString("SIGN_ALIAS"));
				user.setPassKey(rs.getString("PASS_KEY"));
				user.setEmail(rs.getString("EMAIL"));
				user.setPic(rs.getShort("PIC"));
				listUser.add(user);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new EJBException(e);
		}
		return listUser;
    }	       
    
    public void sendDoc() {
    	if (check()) {
    		sendMessage();
    	}
    }
    
    public boolean check() {
    	
    	if (this.listSoglUser.size() == 0) {
    		FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Составте список пользователей для согласования!", ""));
    		return false;
    	}
    	if (this.signUsr == null || this.signUsr.size() == 0) {
    		FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите пользователя для подписания документа!", ""));
    		return false;
    	}
    	if (this.listPoluch.size() == 0) {
    		FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Составте список получателей!", ""));
    		return false;
    	}
    	if (this.themeId == 0) {
    		FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите тему документа!", ""));
    		return false;
    	}
    	if (this.typeId == 0) {
    		FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите тип документа!", ""));
    		return false;
    	}
    	
    	return true;
    }
    
    public void sendMessage() {
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection();
    		// добавляем или обновляем дату исполненияб тему док-та,тип док-та и краткое содержание
    		String sql = "update LINK_DOC SET D_T_REALIZE = ?, THEME_ID = ?, TYPE_ID = ?, SHORT_TEXT = ?, SEND_DOC = 1 "
    				   + "WHERE USER_ID = ? and DOC_ID = ? and ID_PROJECT = ?";// добавляем дату исполнения
			
			try (PreparedStatement ps = conn.prepareStatement(sql);) {
				ps.setTimestamp(1, new Timestamp(this.dateIspoln.getTime()));
				ps.setInt(2, this.themeId);
				ps.setInt(3, this.typeId);
				ps.setString(4, this.selectedDoc.getShortText());
				
				ps.setInt(5, userService.getUser().getUserId());
				ps.setString(6, this.selectedDoc.getDocId());
				ps.setInt(7, this.selectedDoc.getIdProject());
				ps.executeUpdate();
			}
			
			insertListSogl();
			insertUserSign();
			insertListPoluch();
			
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
				try {
					/*System.out.println("isCloseDlgNewDoc "+userService.isCloseDlgNewDoc());
					if (userService.isCloseDlgNewDoc()) {
						userService.setCloseDlgNewDoc(false);
						RequestContext.getCurrentInstance().execute("PF('dlghlp').hide()");
						//RequestContext.getCurrentInstance().closeDialog(null);	
					}*/// else
					FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (SQLException fse) {
				fse.printStackTrace();
			}
		}
    }
    
    public void insertListSogl() {
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection();
    		
    		//Запись в бд списка пользователей для согласования 
			String sql = "insert into SIGN_DOC(NPP_SOGL, USER_ID_OTPR, USER_ID_POL, DOC_ID, SIGN_DOC, ACTION_TYPE, D_T_SEND, READ_DOC, NEXT_USER)" + 
					 "values(?,?,?,?,?,?, CURRENT TIMESTAMP,?,?)";
			try (PreparedStatement ps = conn.prepareStatement(sql);) {
				int npp = 0;
				int usrNext = 0;
				boolean tipOtpr = false;
				for (int i = 0; i < this.listSoglUser.size(); i++) {
					
					if (this.tipOtprDoc == 1) { 
						npp = npp + 1;
						if (tipOtpr == false) {
							usrNext = 1;
						}
					}
					else {
						if (this.listSoglUser.get(i).isSogl() != true) {
							npp = npp + 1; 
							usrNext = 0;
						}
						else usrNext = 1;
					}
					
					if (npp == 0) npp = 1;
					
					ps.setInt(1, npp);
    				ps.setInt(2, userService.getUser().getUserId());
    				ps.setInt(3, this.listSoglUser.get(i).getUserId());
    				ps.setString(4, this.selectedDoc.getDocId());
    				ps.setInt(5, 0);//SIGN_DOC 
    				ps.setInt(6, 1);//ACTION_TYPE = 1 согласование
    				ps.setInt(7, 0);//READ_DOC = 0 документ не прочтен
    				ps.setInt(8, usrNext);

    				ps.executeUpdate();
    				
    				tipOtpr = true;
    				if (tipOtpr == true) {
						usrNext = 0;
					}
				}
			}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД пользователей для согласования", e.getMessage()));
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
    
    public void insertUserSign() {
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection();
    		
    		//Запись пользователя для подписания
			String sql = "insert into SIGN_DOC(NPP_SOGL, USER_ID_OTPR, USER_ID_POL, DOC_ID, SIGN_DOC, ACTION_TYPE, D_T_SEND, READ_DOC, NEXT_USER)" + 
					 "values(?,?,?,?,?,?, CURRENT TIMESTAMP,?,?)";
			try (PreparedStatement ps = conn.prepareStatement(sql);) {

					ps.setInt(1, 1);
    				ps.setInt(2, userService.getUser().getUserId());
    				ps.setInt(3, this.signUsr.get(0).getUserId());
    				ps.setString(4, this.selectedDoc.getDocId());
    				ps.setInt(5, 0);//SIGN_DOC 
    				ps.setInt(6, 2);//ACTION_TYPE = 2 подписание
    				ps.setInt(7, 0);//READ_DOC = 0 документ не прочтен
    				ps.setInt(8, 0);

    				ps.executeUpdate();
					
			}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД пользователя для согласования", e.getMessage()));
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
    
    public void insertListPoluch() {
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection();
    		
    		//Запись в бд списка получателей
			String sql = "insert into SIGN_DOC(NPP_SOGL, USER_ID_OTPR, USER_ID_POL, DOC_ID, SIGN_DOC, ACTION_TYPE, D_T_SEND, READ_DOC, NEXT_USER)" + 
					 "values(?,?,?,?,?,?, CURRENT TIMESTAMP,?,?)";
			try (PreparedStatement ps = conn.prepareStatement(sql);) {
				int npp = 0;
				for (int i = 0; i < this.listPoluch.size(); i++) {
					npp = npp + 1;
															
					ps.setInt(1, npp);
    				ps.setInt(2, userService.getUser().getUserId());
    				ps.setInt(3, this.listPoluch.get(i).getUserId());
    				ps.setString(4, this.selectedDoc.getDocId());
    				ps.setInt(5, 0);//SIGN_DOC 
    				ps.setInt(6, 0);//ACTION_TYPE = 0 получатели
    				ps.setInt(7, 0);//READ_DOC = 0 документ не прочтен
    				ps.setInt(8, 0);

    				ps.executeUpdate();
					
				}
			}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД списка получателей", e.getMessage()));
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
    
	public UserMB getUserService() {
		return userService;
	}

	public void setUserService(UserMB userService) {
		this.userService = userService;
	}

	public Date getDateIspoln() {
		return dateIspoln;
	}

	public void setDateIspoln(Date dateIspoln) {
		this.dateIspoln = dateIspoln;
	}

	public List<User> getListAllUsers() {
		return listAllUsers;
	}

	public void setListAllUsers(List<User> listAllUsers) {
		this.listAllUsers = listAllUsers;
	}

	public LinkDoc getSelectedDoc() {
		return selectedDoc;
	}

	public void setSelectedDoc(LinkDoc selectedDoc) {
		this.selectedDoc = selectedDoc;
	}

	public List<User> getListSoglUser() {
		return listSoglUser;
	}

	public void setListSoglUser(List<User> listSoglUser) {
		this.listSoglUser = listSoglUser;
	}

	public String getSearchOneRadio() {
		return searchOneRadio;
	}

	public void setSearchOneRadio(String searchOneRadio) {
		this.searchOneRadio = searchOneRadio;
	}

	public String getSearchInputTxt() {
		return searchInputTxt;
	}

	public void setSearchInputTxt(String searchInputTxt) {
		this.searchInputTxt = searchInputTxt;
	}

	public List<User> getListSearchUser() {
		return listSearchUser;
	}

	public void setListSearchUser(List<User> listSearchUser) {
		this.listSearchUser = listSearchUser;
	}

	public User getSelectedSearchUser() {
		return selectedSearchUser;
	}

	public void setSelectedSearchUser(User selectedSearchUser) {
		this.selectedSearchUser = selectedSearchUser;
	}

	public int getTipOtprDoc() {
		return tipOtprDoc;
	}

	public void setTipOtprDoc(int tipOtprDoc) {
		this.tipOtprDoc = tipOtprDoc;
	}

	public User getSelectedUsr() {
		return selectedUsr;
	}

	public void setSelectedUsr(User selectedUsr) {
		this.selectedUsr = selectedUsr;
	}

	public boolean isUsrSign() {
		return usrSign;
	}

	public void setUsrSign(boolean usrSign) {
		this.usrSign = usrSign;
	}

	public List<User> getSignUsr() {
		return signUsr;
	}

	public void setSignUsr(List<User> signUsr) {
		this.signUsr = signUsr;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public boolean isPoluchateli() {
		return poluchateli;
	}

	public void setPoluchateli(boolean poluchateli) {
		this.poluchateli = poluchateli;
	}

	public List<User> getListPoluch() {
		return listPoluch;
	}

	public void setListPoluch(List<User> listPoluch) {
		this.listPoluch = listPoluch;
	}

	public int getThemeId() {
		return themeId;
	}

	public void setThemeId(int themeId) {
		this.themeId = themeId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public List<TypeOrThemeDoc> getListThemeDoc() {
		return listThemeDoc;
	}

	public void setListThemeDoc(List<TypeOrThemeDoc> listThemeDoc) {
		this.listThemeDoc = listThemeDoc;
	}

	public List<TypeOrThemeDoc> getListTypeDoc() {
		return listTypeDoc;
	}

	public void setListTypeDoc(List<TypeOrThemeDoc> listTypeDoc) {
		this.listTypeDoc = listTypeDoc;
	}

	public List<User> getListViewUserSogl() {
		return listViewUserSogl;
	}

	public void setListViewUserSogl(List<User> listViewUserSogl) {
		this.listViewUserSogl = listViewUserSogl;
	}

	public List<User> getListViewUserPoluch() {
		return listViewUserPoluch;
	}

	public void setListViewUserPoluch(List<User> listViewUserPoluch) {
		this.listViewUserPoluch = listViewUserPoluch;
	}

	public User getRemoveUser() {
		return removeUser;
	}

	public void setRemoveUser(User removeUser) {
		this.removeUser = removeUser;
	}

	public NewDocMB getNewDocMB() {
		return newDocMB;
	}

	public void setNewDocMB(NewDocMB newDocMB) {
		this.newDocMB = newDocMB;
	}
	
}
