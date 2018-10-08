package kz.railways.models;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
import org.primefaces.extensions.component.ckeditor.CKEditor;

import com.itextpdf.io.util.SystemUtil;

import kz.railways.entities.LinkDoc;
import models.PdfMB;
import models.UserMB;
import views.HelperUtils;

@ManagedBean(name = "newDocMB")
@ViewScoped
public class NewDocMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	
	private Connection conn;
	
	@ManagedProperty("#{userMB}")
    private UserMB userService;	
	
	public LinkDoc newDoc;
	
	private String text;
	
	private boolean saveAndSendDoconclick = false;
	
	@PostConstruct
	public void init() {
		this.newDoc = new LinkDoc();
	}

	public LinkDoc getNewDoc() {
		return newDoc;
	}

	public void setNewDoc(LinkDoc newDoc) {
		this.newDoc = newDoc;
	}

	public void saveDoc() {
		try {
			if (this.newDoc.getNameDoc() == null || this.newDoc.getNameDoc().equals("")) {
				FacesContext.getCurrentInstance().addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите имя документа!",  ""));
			} else if (this.newDoc.getIdProject() == 0) {
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите проект!", ""));
			}
			else {
				//======================= путь для Windows =======================
				String path = "C:\\SPD_WEB\\" + userService.getUser().getUserName() + "\\";
				Timestamp dt = new Timestamp(System.currentTimeMillis());			
				path = path + dt.toString().substring(0, 4) + "\\" + dt.toString().substring(5, 7) + "\\" + dt.toString().substring(8, 10);
				new File(path).mkdirs();//создание пути			
				path = path + "\\" + this.newDoc.getNameDoc() + ".pdf";
				//================================================================
			
				this.newDoc.setLinkDoc(path);
				this.newDoc.setDtCreate(dt);
				this.newDoc.setDocId(dt.toString().substring(0, 4)   + dt.toString().substring(5, 7)   + dt.toString().substring(8, 10) + 
								 	 dt.toString().substring(11, 13) + dt.toString().substring(14, 16) + dt.toString().substring(17, 19));
				System.out.println(this.newDoc.getTextDoc());
				System.out.println(path);
				PdfMB.htmlToPdf(this.newDoc.getTextDoc(), path);//конвертация файла из HTML в PDF и запись на диск	
				
				addNewDoc(); //запись нового документа в бд
			//	userService.getMainListDoc(userService.getUser().getUserId());//обновляем список мои документы
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка создания документа!",  ""));
			
		}
		
	}
	
	public void saveAndSendDoc() {		
		if (this.newDoc.getNameDoc() == null || this.newDoc.getNameDoc().equals("")) {
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите имя документа!",  ""));
		} else if (this.newDoc.getIdProject() == 0) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите проект!", ""));
		}
		else {
			saveAndSendDoconclick = true;
			saveDoc();
		//	RequestContext.getCurrentInstance().execute("PF('dlg1').hide()");
			userService.setSelectedDoc(selectDocDB(userService.getUser().getUserId(), this.newDoc.getDocId(), this.newDoc.getIdProject()));	
			try {	
				FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/saveAndSendDoc.xhtml");			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//	RequestContext.getCurrentInstance().openDialog("/pages/saveAndSendDoc", HelperUtils.dfOptions(738, 700), null);
		//	userService.setCloseDlgNewDoc(true);
		//	RequestContext.getCurrentInstance().closeDialog(null);
		}
	}
	
	public void closeNewDocForm() {
	//	RequestContext.getCurrentInstance().closeDialog(null);
		try {	
			FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//запись нового документа в бд
    public void addNewDoc() {
    	try {
    		this.conn = dataSource.getConnection();
    		
    		String sql = "insert into LINK_DOC (USER_ID, NAME_DOC, LINK_DOC, DOC_ID, ID_PROJECT, D_T_CREATE, SHORT_TEXT, THEME_ID, TYPE_ID)" + 
    					 "values(?,?,?,?,?,?,?, 999, 999)";
    		
    		try (PreparedStatement ps = conn.prepareStatement(sql);) {
    			ps.setInt(1, userService.getUser().getUserId());
    			ps.setString(2, this.newDoc.getNameDoc());
    			ps.setString(3, this.newDoc.getLinkDoc());
    			ps.setString(4, this.newDoc.getDocId());
    			ps.setInt(5, this.newDoc.getIdProject());
    			ps.setTimestamp(6, new Timestamp(this.newDoc.getDtCreate().getTime()));
    			ps.setString(7, this.newDoc.getShortText());
    			//ps.setString(8, this.newDoc.getRegNumber()); поле в базе автоинкр.
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
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка при записи в БД!", e.getMessage()));
			throw (EJBException) new EJBException(e).initCause(e); 
		}
		finally {
			try {
				conn.close();
				try {
					userService.getUser().setMainListDoc(userService.getMainListDoc());
					if (!saveAndSendDoconclick)
						//RequestContext.getCurrentInstance().closeDialog(null);
						FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SQLException fse) {
				fse.printStackTrace();
			}
		}
    }
    
    public LinkDoc selectDocDB(int userId, String docId, int idProject) {
    	LinkDoc ls = new LinkDoc();
    	
    	String sql = "select LINK_DOC.REG_N, LINK_DOC.USER_ID, LINK_DOC.NAME_DOC, LINK_DOC.LINK_DOC, LINK_DOC.DOC_ID, LINK_DOC.D_T_CREATE, " +
    				 "LINK_DOC.D_T_REALIZE, LINK_DOC.SHORT_TEXT, LINK_DOC.ANSWER_INCOM, " +
    				 "PROJECTS.ID_PROJECT, PROJECTS.PROJECT, PROJECTS.MNKD_PROJ, " +
    				 "THEME_DOC.THEME_ID, THEME_DOC.THEME, TYPE_DOC.TYPE_ID, TYPE_DOC.TYPE " +
    				 "from LINK_DOC, PROJECTS, THEME_DOC, TYPE_DOC where " +
    				 "LINK_DOC.USER_ID LIKE ? AND LINK_DOC.DOC_ID LIKE ? AND LINK_DOC.ID_PROJECT LIKE ? AND " +
    				 "LINK_DOC.ID_PROJECT = PROJECTS.ID_PROJECT " +
    				 "AND THEME_DOC.THEME_ID = LINK_DOC.THEME_ID AND TYPE_DOC.TYPE_ID = LINK_DOC.TYPE_ID ";
    	
    	try (Connection conn = dataSource.getConnection();
				 PreparedStatement ps = conn.prepareStatement(sql);) {
			
			ps.setInt(1, userId);
			ps.setString(2, docId);
			ps.setInt(3, idProject);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {				
				ls.setRegNumber(rs.getInt("REG_N"));
				ls.setNameDoc(rs.getString("NAME_DOC"));
				ls.setLinkDoc(rs.getString("LINK_DOC"));
				ls.setDocId(rs.getString("DOC_ID"));
				ls.setDtCreate(rs.getDate("D_T_CREATE"));
				ls.setDtRealize(rs.getDate("D_T_REALIZE"));
				ls.setShortText(rs.getString("SHORT_TEXT"));
				ls.setAnswerIncom(rs.getString("ANSWER_INCOM"));
				ls.setIdProject(rs.getInt("ID_PROJECT"));
				ls.setProject(rs.getString("PROJECT"));
				ls.setMnkdProj(rs.getString("MNKD_PROJ"));
				ls.setThemeId(rs.getInt("THEME_ID"));
				ls.setTheme(rs.getString("THEME"));
				ls.setTypeId(rs.getInt("TYPE_ID"));
				ls.setType(rs.getString("TYPE"));
			}
			rs.close();
			
    	} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
    	
    	return ls;
    }
    
    public UserMB getUserService() {
		return userService;
	}

	public void setUserService(UserMB userService) {
		this.userService = userService;
	}
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isSaveAndSendDoconclick() {
		return saveAndSendDoconclick;
	}

	public void setSaveAndSendDoconclick(boolean saveAndSendDoconclick) {
		this.saveAndSendDoconclick = saveAndSendDoconclick;
	}
}
