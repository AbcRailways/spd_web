package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.*;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


import kz.railways.entities.LinkDoc;
import kz.railways.entities.User;
import kz.railways.entities.UserProject;
import views.HelperUtils;


@ManagedBean(name = "userMB")
@SessionScoped
public class UserMB implements Serializable {

	private static final long serialVersionUID = -4118144519094007627L;
		
	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	
	private Connection conn;
	
	private User user;
	private String userName;
	private String name;
	private String info;
	private LinkDoc selectedMain;
	private LinkDoc selectedSend;
	private LinkDoc selectedIncoming;
	private LinkDoc selectedOtkazDoc;
	private LinkDoc selectedOther;
	
	
	private LinkDoc selectedDoc;
	private String encodedFileName;
	private StreamedContent streamedContent;	
	
	private int newMessage;
	private int newOtkaz;
	private String incomMsg;
	private String other;
	private String otkazMsg;
	
	private boolean openMainDocs = false;// для отображения datatable моих документов
	private boolean openIncomingDocs = false;// для отображения datatable входящих
	private boolean openSendDocs = false;// для отображения datatable отправленных
	private boolean openOtherDocs = false;// для отображения datatable остальных (работает когда у пользователя не прописан один из проектов)
	private boolean openOtkazDocs = false;// для отображения datatable при отказе документа
	private boolean pnl = false;//для отображения panelGrid
//	public boolean closeDlgNewDoc = false;
	
	@PostConstruct
    public void init() {
		System.out.println("Users init---");
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		this.userName = context.getUserPrincipal().getName();
		System.out.println(this.userName);
		this.user = find(this.userName);
		System.out.println("Вход - "+(isUserAdmin() == true ? "ADMIN " : "USER ") + userName);
		System.out.println("--------------");
		this.name = this.user.getFullName() + " " + this.user.getSurname();
		this.info = this.user.getPosition() + " Отдел "+ this.user.getDepartment() + ". Тел: " + this.user.getnPhone();		
		this.pnl = true;
		checkNewMessage();
	}

	
	public boolean isUserAdmin() {
		return getRequest().isUserInRole("ADMIN");
	}

	public String logOut() {
		System.out.println("Выход - " + this.userName);
		getRequest().getSession().invalidate();
		return "logout";
	}
	

	private HttpServletRequest getRequest() {
		return (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
	}
	
	public void openMainDoc() throws IOException {
		this.selectedMain = null;				
		this.openMainDocs = true;
		this.openIncomingDocs = false;
		this.openSendDocs = false;
		this.openOtherDocs = false;
		this.openOtkazDocs = false;
		this.pnl = false;
		this.user.setMainListDoc(getMainListDoc());
		RequestContext.getCurrentInstance().update("rightForm:pnlForm");
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
	}
	
	public void openIncomingDoc() throws IOException {
		this.selectedIncoming = null;
		this.openMainDocs = false;
		this.openIncomingDocs = true;
		this.openSendDocs = false;
		this.openOtherDocs = false;
		this.openOtkazDocs = false;
		this.pnl = false;
		checkNewMessage();
		RequestContext.getCurrentInstance().update("rightForm:pnlForm");
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
	}
	
	public void openSendDoc() throws IOException {
		this.selectedSend = null;
		this.openMainDocs = false;
		this.openIncomingDocs = false;
		this.openSendDocs = true;
		this.openOtherDocs = false;
		this.openOtkazDocs = false;
		this.pnl = false;
		this.user.setListSend(getSend(this.user.getUserId()));
		RequestContext.getCurrentInstance().update("rightForm:pnlForm");
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
	}
	
	public void openOtherDoc() throws IOException {
		this.selectedOther = null;
		this.openMainDocs = false;
		this.openIncomingDocs = false;
		this.openSendDocs = false;
		this.openOtherDocs = true;
		this.openOtkazDocs = false;
		this.pnl = false;
		this.user.setListOther(getOther(user.getUserId()));
		RequestContext.getCurrentInstance().update("rightForm:pnlForm");
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
	}
	public void openOtkazDoc() throws IOException {
		this.selectedOtkazDoc = null;
		this.openMainDocs = false;
		this.openIncomingDocs = false;
		this.openSendDocs = false;
		this.openOtherDocs = false;
		this.openOtkazDocs = true;
		this.pnl = false;
		checkNewMessage();
		RequestContext.getCurrentInstance().update("rightForm:pnlForm");
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
	}
	//===============================Открытие выбранного файла============================================================
	
	public void onRowSelectMain(SelectEvent event) {
		setSelectedDoc(this.selectedMain);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/sendDoc.xhtml");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onRowSelectIncoming(SelectEvent event) {
		setSelectedDoc(this.selectedIncoming);
		int index = this.user.getListIncoming().indexOf(this.selectedDoc);
		this.user.getListIncoming().get(index).setReadDoc(1);
		updateReadDoc();
		incomingMessages();
		titleTab();
		try {
			createStream(this.selectedDoc.getNameDoc() + ".pdf");
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/viewPdf.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onRowSelectSend(SelectEvent event) {
		setSelectedDoc(this.selectedSend);
	try {
		FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/docProgress.xhtml");
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	public void onRowSelectOtkaz(SelectEvent event) {
		setSelectedDoc(this.selectedOtkazDoc);
		int index = this.user.getListOtkaz().indexOf(this.selectedDoc);
		this.user.getListOtkaz().get(index).setReadDoc(1);
		updateReadDoc();
		incomingMessages();
		titleTab();
		System.out.println("Проверка индекса "+index);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/otkaz.xhtml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void onRowSelectOther(SelectEvent event) {
		setSelectedDoc(this.selectedOther);
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/moreInfo.xhtml");			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void showModalDlgPdf() {
		createStream(this.selectedDoc.getNameDoc() + ".pdf");
		RequestContext.getCurrentInstance().openDialog("/pages/pdfView", HelperUtils.dfOptions(930, 800), null);
	}
	
	private StreamedContent createStream(String selectedFileName) {
    	try {
    		System.out.println("selected name "+selectedFileName);
			encodedFileName = URLEncoder.encode(selectedFileName, "UTF-8").replace("+", "%20");
			System.out.println("encoded file name "+encodedFileName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	streamedContent = new DefaultStreamedContent(getData(), "application/pdf", encodedFileName);
		return streamedContent;
    }
	
	private InputStream getData() {

    	System.out.println("link doc "+this.selectedDoc.getLinkDoc());
		File file = new File(this.selectedDoc.getLinkDoc());
		System.out.println("file "+file);
		InputStream is = null;	
		try {
			if (file.exists()) {
				is = new FileInputStream(file);
				System.out.println("file init--- "+is);
			} /*else {
				//FacesContext.getCurrentInstance().addMessage(null,
				//		new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка файл не найден!", ""));
			}*/
					
		} catch (FileNotFoundException e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка файл не найден!", ""));
			e.printStackTrace();
		}
    	return is;				
	}
	
	public String generateRandomIdForNotCaching() {
		return java.util.UUID.randomUUID().toString();
	}
	
	public  void updateReadDoc() {
    	try {
    		this.conn = dataSource.getConnection();
    		
    		String sql = "update SIGN_DOC set READ_DOC = 1 where USER_ID_OTPR = ? and USER_ID_POL = ? and DOC_ID = ?";
    		
    		try (PreparedStatement ps = this.conn.prepareStatement(sql);) {
    			ps.setInt(1, this.selectedDoc.getUserIdOtpr());
    			ps.setInt(2, this.selectedDoc.getUserIdPol());
    			ps.setString(3, this.selectedDoc.getDocId());
    			ps.executeUpdate();
    		}
    		
    	} catch (SQLException e) {
			try {
				this.conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw (EJBException) new EJBException(e).initCause(e); 
		}
		finally {
			try {
				this.conn.close();
			} catch (SQLException fse) 
			{
				fse.printStackTrace();
			}
		}
	}
	//====================================================================================================================
	
	
	//проверка входящих сообщений
	public void incomingMessages() {
		setNewMessage(0);		
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		list = getIncoming(this.user.getUserId());		
		user.getListIncoming().clear();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getReadDoc() == 0 && list.get(i).getNextUser() == 1) {
				setNewMessage(getNewMessage() + 1);	
				user.getListIncoming().add(list.get(i));
			} else if (list.get(i).getReadDoc() == 1 && list.get(i).getNextUser() == 1) {
				user.getListIncoming().add(list.get(i));
			}
		}
		
		setNewOtkaz(0);
		list = new ArrayList<LinkDoc>();		
		list = listOtkaz();		
		user.getListOtkaz().clear();
		
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getReadDoc() == 0) {
				setNewOtkaz(getNewOtkaz() + 1);					
			}
			user.getListOtkaz().add(list.get(i));
		}		
	}
	
	//заголовок входящих
	public void titleTab() {
		this.incomMsg = "Входящие";	
		this.otkazMsg = "Отказанные";
		if (this.newMessage != 0) {
			this.incomMsg = this.incomMsg + ":  " + this.newMessage;	
		}
		if (this.newOtkaz != 0) {
			this.otkazMsg = this.otkazMsg + ":  " + this.newOtkaz;	
		}
	}
	
	
	
	public void checkNewMessage() {
		incomingMessages();
		titleTab();
		if (this.newMessage != 0) {						
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "У вас " + this.newMessage + " входящих сообщений!",  ""));
			if (this.openIncomingDocs == true) {
				RequestContext.getCurrentInstance().update("rightForm:pnlForm");
			}
		}
		
		if (this.newOtkaz != 0) {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Вам отказано в документе!",  ""));
			if (this.openIncomingDocs == true) {
				RequestContext.getCurrentInstance().update("rightForm:pnlForm");
			}
		}
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public User getUser() {
		return this.user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	//загрузка информации о пользователе
	public User find(String name) {
		User user = new User();
		
		String sql = "select USERS.USER_ID, USERS.USERNAME, " +
					 "USER_INFO.SURNAME, USER_INFO.FULL_NAME, USER_INFO.POSITION_U, " +
					 "USER_INFO.DEPARTMENT, USER_INFO.N_PHONE, USER_INFO.SIGN_ALIAS, " +
					 "USER_INFO.PASS_KEY, USER_INFO.EMAIL, USER_INFO.PIC " +
					 "from USERS, USER_INFO " +
					 "where USERNAME like ? AND USER_INFO.USER_ID=USERS.USER_ID";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				user.setUserId(rs.getInt("USER_ID"));
				user.setUserName(rs.getString("USERNAME"));
				user.setSurname(rs.getString("SURNAME"));
				user.setFullName(rs.getString("FULL_NAME"));
				user.setPosition(rs.getString("POSITION_U"));
				user.setDepartment(rs.getString("DEPARTMENT"));
				user.setnPhone(rs.getString("N_PHONE"));
				user.setSignAlias(rs.getString("SIGN_ALIAS"));
				user.setPassKey(rs.getString("PASS_KEY"));
				user.setEmail(rs.getString("EMAIL"));
				user.setPic(rs.getInt("PIC"));
			}
			
			rs.close();			
			user.setListUserProject(this.getUserProject(user));//загрузка списка проектов пользователя
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return user;
	}
	
	public List<UserProject> getUserProject(User user) {
		List<UserProject> list = new ArrayList<UserProject>();
		
		String sql="select USERS_PROJECT.USER_ID, USERS_PROJECT.ID_PROJECT, PROJECTS.ID_PROJECT, PROJECTS.PROJECT, PROJECTS.MNKD_PROJ "
				+ "from USERS_PROJECT, PROJECTS where USERS_PROJECT.USER_ID LIKE ? AND USERS_PROJECT.ID_PROJECT = PROJECTS.ID_PROJECT";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, user.getUserId());
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				UserProject up = new UserProject();
				up.setIdProject(rs.getInt("ID_PROJECT"));
				up.setProject(rs.getString("PROJECT"));
				up.setMnkdProj(rs.getString("MNKD_PROJ"));
				list.add(up);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
		
	public List<LinkDoc> getMainListDoc() {
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		
		String sql = "select USERS_PROJECT.USER_ID, USERS_PROJECT.ID_PROJECT, " +
					 "PROJECTS.ID_PROJECT, PROJECTS.PROJECT, PROJECTS.MNKD_PROJ, " +
					 "LINK_DOC.REG_N, LINK_DOC.USER_ID, LINK_DOC.NAME_DOC, LINK_DOC.LINK_DOC, LINK_DOC.DOC_ID, LINK_DOC.D_T_CREATE, " +
					 "LINK_DOC.D_T_REALIZE, LINK_DOC.SHORT_TEXT, LINK_DOC.ANSWER_INCOM, " +
					 "THEME_DOC.THEME_ID, THEME_DOC.THEME, TYPE_DOC.TYPE_ID, TYPE_DOC.TYPE " +
					 "from USERS_PROJECT, PROJECTS, LINK_DOC, THEME_DOC, TYPE_DOC where USERS_PROJECT.USER_ID LIKE ? " +
					 "AND USERS_PROJECT.ID_PROJECT = PROJECTS.ID_PROJECT " +
					 "AND LINK_DOC.USER_ID = USERS_PROJECT.USER_ID AND LINK_DOC.ID_PROJECT = USERS_PROJECT.ID_PROJECT AND LINK_DOC.SEND_DOC = 0 " +
					 "AND THEME_DOC.THEME_ID = LINK_DOC.THEME_ID AND TYPE_DOC.TYPE_ID = LINK_DOC.TYPE_ID " +
					 "order by LINK_DOC.D_T_CREATE desc";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, this.user.getUserId());
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LinkDoc ls = new LinkDoc();	
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setProject(rs.getString("PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));
				ls.setRegNumber(rs.getInt("REG_N"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setDtCreate(rs.getDate("D_T_CREATE"));
				ls.setDtRealize(rs.getDate("D_T_REALIZE"));
				ls.setShortText(rs.getString("SHORT_TEXT"));
				ls.setAnswerIncom(rs.getString("ANSWER_INCOM"));
				ls.setThemeId(rs.getInt("THEME_ID"));
				ls.setTheme(rs.getString("THEME"));
				ls.setTypeId(rs.getInt("TYPE_ID"));
				ls.setType(rs.getString("TYPE"));
				list.add(ls);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
		
	public List<LinkDoc> getIncoming(int userId) {
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		
		String sql = "select SIGN_DOC.NPP_SOGL, SIGN_DOC.USER_ID_OTPR, SIGN_DOC.USER_ID_POL, SIGN_DOC.SIGN_DOC, SIGN_DOC.NEXT_USER, " +
					 "SIGN_DOC.ACTION_TYPE, SIGN_DOC.D_T_SEND, SIGN_DOC.READ_DOC, " +
					 "LINK_DOC.NAME_DOC, LINK_DOC.DOC_ID, LINK_DOC.LINK_DOC, LINK_DOC.SHORT_TEXT, " +
					 "USERS_PROJECT.ID_PROJECT, " +
					 "PROJECTS.PROJECT, PROJECTS.MNKD_PROJ, " +
					 "USER_INFO.SURNAME, USER_INFO.FULL_NAME " +
					 "from SIGN_DOC, LINK_DOC, USERS_PROJECT, PROJECTS, USER_INFO " + 
					 "where SIGN_DOC.USER_ID_POL like ? and LINK_DOC.USER_ID = SIGN_DOC.USER_ID_OTPR and SIGN_DOC.DOC_ID = LINK_DOC.DOC_ID and " +
					 "USERS_PROJECT.USER_ID = SIGN_DOC.USER_ID_POL and USERS_PROJECT.ID_PROJECT = LINK_DOC.ID_PROJECT and " +
					 "PROJECTS.ID_PROJECT = USERS_PROJECT.ID_PROJECT and " +
					 "USER_INFO.USER_ID = SIGN_DOC.USER_ID_OTPR order by SIGN_DOC.D_T_SEND desc";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LinkDoc ls = new LinkDoc();
				ls.setNppSogl(rs.getInt("NPP_SOGL"));
				ls.setUserIdOtpr(rs.getInt("USER_ID_OTPR"));
				ls.setUserIdPol(rs.getInt("USER_ID_POL"));
				ls.setSignDoc(rs.getInt("SIGN_DOC"));
				ls.setActionType(rs.getInt("ACTION_TYPE"));
				ls.setDtSend(rs.getDate("D_T_SEND"));
				ls.setReadDoc(rs.getInt("READ_DOC"));	
				ls.setNextUser(rs.getInt("NEXT_USER"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setShortText(rs.getString("SHORT_TEXT"));				
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setProject(rs.getString("PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));
				ls.setSurname(rs.getString("SURNAME"));
				ls.setFullName(rs.getString("FULL_NAME"));																
				list.add(ls);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	public List<LinkDoc> getSend(int userId) {
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		
		String sql = "select SIGN_DOC.DOC_ID, DATE(SIGN_DOC.D_T_SEND) as D_T_SEND, LINK_DOC.NAME_DOC, LINK_DOC.LINK_DOC, USERS_PROJECT.ID_PROJECT, PROJECTS.MNKD_PROJ " +
					 "from SIGN_DOC, LINK_DOC, USERS_PROJECT, PROJECTS " +
					 "where SIGN_DOC.USER_ID_OTPR = ? and " +
					 "LINK_DOC.USER_ID = SIGN_DOC.USER_ID_OTPR and LINK_DOC.DOC_ID = SIGN_DOC.DOC_ID and " +
					 "USERS_PROJECT.USER_ID = SIGN_DOC.USER_ID_OTPR and USERS_PROJECT.ID_PROJECT = LINK_DOC.ID_PROJECT and " +
					 "PROJECTS.ID_PROJECT = USERS_PROJECT.ID_PROJECT " +
					 "group by SIGN_DOC.DOC_ID, LINK_DOC.NAME_DOC, LINK_DOC.LINK_DOC, PROJECTS.MNKD_PROJ, USERS_PROJECT.ID_PROJECT, DATE(SIGN_DOC.D_T_SEND) " +
					 "order by DATE(SIGN_DOC.D_T_SEND) desc";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LinkDoc ls = new LinkDoc();
				
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setDtSend(rs.getDate("D_T_SEND"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));																	
				list.add(ls);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}

	public List<LinkDoc> listOtkaz() {
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		
		String sql = "select SIGN_DOC.USER_ID_OTPR, SIGN_DOC.USER_ID_POL, SIGN_DOC.SIGN_DOC, SIGN_DOC.NEXT_USER, " + 
					 "SIGN_DOC.ACTION_TYPE, SIGN_DOC.D_T_SEND, SIGN_DOC.READ_DOC, SIGN_DOC.SHORT_TEXT, " + 
					 "LINK_DOC.NAME_DOC, LINK_DOC.DOC_ID, LINK_DOC.LINK_DOC, LINK_DOC.REG_N, " + 
					 "USERS_PROJECT.ID_PROJECT, " + 
					 "PROJECTS.PROJECT, PROJECTS.MNKD_PROJ, " + 
					 "USER_INFO.SURNAME, USER_INFO.FULL_NAME, USER_INFO.POSITION_U, USER_INFO.DEPARTMENT, USER_INFO.N_PHONE " + 
					 "from SIGN_DOC, LINK_DOC, USERS_PROJECT, PROJECTS, USER_INFO " + 
					 "where SIGN_DOC.USER_ID_POL like ? and SIGN_DOC.SIGN_DOC = 4 and SIGN_DOC.ACTION_TYPE = 4 and LINK_DOC.USER_ID = SIGN_DOC.USER_ID_POL " + 
					 "and SIGN_DOC.DOC_ID = LINK_DOC.DOC_ID " + 
					 "and USERS_PROJECT.USER_ID = SIGN_DOC.USER_ID_POL and USERS_PROJECT.ID_PROJECT = LINK_DOC.ID_PROJECT " + 
					 "and PROJECTS.ID_PROJECT = USERS_PROJECT.ID_PROJECT " + 
					 "and USER_INFO.USER_ID = SIGN_DOC.USER_ID_OTPR order by SIGN_DOC.D_T_SEND desc";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, user.getUserId());
			
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LinkDoc ls = new LinkDoc();
				ls.setUserIdOtpr(rs.getInt("USER_ID_OTPR"));
				ls.setUserIdPol(rs.getInt("USER_ID_POL"));
				ls.setSignDoc(rs.getInt("SIGN_DOC"));
				ls.setNextUser(rs.getInt("NEXT_USER"));
				ls.setActionType(rs.getInt("ACTION_TYPE"));
				ls.setDtSend(rs.getDate("D_T_SEND"));
				ls.setReadDoc(rs.getInt("READ_DOC"));
				ls.setShortText(rs.getString("SHORT_TEXT"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setRegNumber(rs.getInt("REG_N"));
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setProject(rs.getString("PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));
				ls.setSurname(rs.getString("SURNAME"));
				ls.setFullName(rs.getString("FULL_NAME"));
				ls.setPosition(rs.getString("POSITION_U"));
				ls.setDepartment(rs.getString("DEPARTMENT"));
				ls.setnPhone(rs.getString("N_PHONE"));		
				
				list.add(ls);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	
	public List<LinkDoc> getOther(int userId) {
		List<LinkDoc> list = new ArrayList<LinkDoc>();
		
		String sql = "select  PROJECTS.ID_PROJECT, PROJECTS.PROJECT, PROJECTS.MNKD_PROJ,"  
					 +" LINK_DOC.REG_N, LINK_DOC.USER_ID, LINK_DOC.NAME_DOC, LINK_DOC.LINK_DOC, LINK_DOC.DOC_ID, LINK_DOC.D_T_CREATE,"  
					 +" LINK_DOC.D_T_REALIZE, LINK_DOC.SHORT_TEXT, LINK_DOC.ANSWER_INCOM,"  
					 +" THEME_DOC.THEME_ID, THEME_DOC.THEME, TYPE_DOC.TYPE_ID, TYPE_DOC.TYPE"  
					 +" from PROJECTS, LINK_DOC, THEME_DOC, TYPE_DOC where LINK_DOC.USER_ID=?"					 
					 +" AND LINK_DOC.ID_PROJECT not in (select ID_PROJECT from USERS_PROJECT where USER_ID=?)"
					 +" AND THEME_DOC.THEME_ID = LINK_DOC.THEME_ID AND TYPE_DOC.TYPE_ID = LINK_DOC.TYPE_ID" 
					 +" and PROJECTS.ID_PROJECT=LINK_DOC.ID_PROJECT"					 
					 +" order by LINK_DOC.D_T_CREATE desc";
		
		try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, userId);
			ps.setInt(2, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				LinkDoc ls = new LinkDoc();	
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setProject(rs.getString("PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));
				ls.setRegNumber(rs.getInt("REG_N"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setDtCreate(rs.getDate("D_T_CREATE"));
				ls.setDtRealize(rs.getDate("D_T_REALIZE"));
				ls.setShortText(rs.getString("SHORT_TEXT"));
				ls.setAnswerIncom(rs.getString("ANSWER_INCOM"));
				ls.setThemeId(rs.getInt("THEME_ID"));
				ls.setTheme(rs.getString("THEME"));
				ls.setTypeId(rs.getInt("TYPE_ID"));
				ls.setType(rs.getString("TYPE"));
				list.add(ls);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return list;
	}
	
	public LinkDoc getSelectedMain() {
		return selectedMain;
	}

	public void setSelectedMain(LinkDoc selectedMain) {
		this.selectedMain = selectedMain;
	}	

	public String getEncodedFileName() {
		return encodedFileName;
	}

	public void setEncodedFileName(String encodedFileName) {
		this.encodedFileName = encodedFileName;
	}

	public StreamedContent getStreamedContent() {
		return streamedContent;
	}

	public void setStreamedContent(StreamedContent streamedContent) {
		this.streamedContent = streamedContent;
	}

	public LinkDoc getSelectedDoc() {
		return selectedDoc;
	}

	public void setSelectedDoc(LinkDoc selectedDoc) {
		this.selectedDoc = selectedDoc;
	}

	public int getNewMessage() {
		return newMessage;
	}

	public void setNewMessage(int newMessage) {
		this.newMessage = newMessage;
	}

	public String getIncomMsg() {
		return incomMsg;
	}

	public void setIncomMsg(String incomMsg) {
		this.incomMsg = incomMsg;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public boolean isOpenMainDocs() {
		return openMainDocs;
	}

	public void setOpenMainDocs(boolean openMainDocs) {
		this.openMainDocs = openMainDocs;
	}

	public boolean isOpenIncomingDocs() {
		return openIncomingDocs;
	}

	public void setOpenIncomingDocs(boolean openIncomingDocs) {
		this.openIncomingDocs = openIncomingDocs;
	}

	public boolean isOpenSendDocs() {
		return openSendDocs;
	}

	public void setOpenSendDocs(boolean openSendDocs) {
		this.openSendDocs = openSendDocs;
	}

	public boolean isOpenOtherDocs() {
		return openOtherDocs;
	}

	public void setOpenOtherDocs(boolean openOtherDocs) {
		this.openOtherDocs = openOtherDocs;
	}

	public boolean isPnl() {
		return pnl;
	}

	public void setPnl(boolean pnl) {
		this.pnl = pnl;
	}

	public LinkDoc getSelectedSend() {
		return selectedSend;
	}

	public void setSelectedSend(LinkDoc selectedSend) {
		this.selectedSend = selectedSend;
	}

	public LinkDoc getSelectedIncoming() {
		return selectedIncoming;
	}

	public void setSelectedIncoming(LinkDoc selectedIncoming) {
		this.selectedIncoming = selectedIncoming;
	}

	public String getOtkazMsg() {
		return otkazMsg;
	}

	public void setOtkazMsg(String otkazMsg) {
		this.otkazMsg = otkazMsg;
	}

	public int getNewOtkaz() {
		return newOtkaz;
	}

	public void setNewOtkaz(int newOtkaz) {
		this.newOtkaz = newOtkaz;
	}

	public boolean isOpenOtkazDocs() {
		return openOtkazDocs;
	}

	public void setOpenOtkazDocs(boolean openOtkazDocs) {
		this.openOtkazDocs = openOtkazDocs;
	}

	public LinkDoc getSelectedOtkazDoc() {
		return selectedOtkazDoc;
	}

	public void setSelectedOtkazDoc(LinkDoc selectedOtkazDoc) {
		this.selectedOtkazDoc = selectedOtkazDoc;
	}

	public LinkDoc getSelectedOther() {
		return selectedOther;
	}

	public void setSelectedOther(LinkDoc selectedOther) {
		this.selectedOther = selectedOther;
	}


	/*public boolean isCloseDlgNewDoc() {
		return closeDlgNewDoc;
	}


	public void setCloseDlgNewDoc(boolean closeDlgNewDoc) {
		this.closeDlgNewDoc = closeDlgNewDoc;
	}*/

}
