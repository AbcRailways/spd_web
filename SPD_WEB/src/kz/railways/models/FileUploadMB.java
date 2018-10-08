package kz.railways.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import org.primefaces.event.FileUploadEvent;

import models.UserMB;

@ManagedBean(name = "fileUploadMB")
@ViewScoped
public class FileUploadMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	
	private Connection conn;
	
	@ManagedProperty("#{userMB}")
    private UserMB userService;

	private int idProject;
	
	@PostConstruct
	public void init() {
		
	}
	
	public void checkIdProject() {
		System.out.println("Загрузка файла в ручную idProject = " + idProject);	
	}
	
	public void fileUpload(FileUploadEvent event) throws IOException {
		if (idProject == 0) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Укажите проект!", ""));
			RequestContext.getCurrentInstance().update("formUpload:fileUploadPnl");
		} else {
		//======================= путь для Windows =======================
		String path = "C:\\SPD_WEB\\" + userService.getUser().getUserName() + "\\";
		Timestamp dt = new Timestamp(System.currentTimeMillis());			
		path = path + dt.toString().substring(0, 4) + "\\" + dt.toString().substring(5, 7) + "\\" + dt.toString().substring(8, 10);		
		new File(path).mkdirs();//создание пути			
		path = path + "\\" + event.getFile().getFileName();
		//================================================================
		
	    System.out.println(path);
	    File file = new File(path);
	    	        
	    InputStream is = event.getFile().getInputstream();
	    OutputStream out = new FileOutputStream(file);
	    byte buf[] = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0)
	        out.write(buf, 0, len);    
	    is.close();
	    out.close();
	    addNewDoc(event, path, dt.toString().substring(0, 4)   + dt.toString().substring(5, 7)   + dt.toString().substring(8, 10) + 
				 dt.toString().substring(11, 13) + dt.toString().substring(14, 16) + dt.toString().substring(17, 19), dt, idProject);
		}
	    
	}
	
	
	//запись нового документа в бд
    public void addNewDoc(FileUploadEvent event, String path, String docId, Timestamp dt, int idProject) {
    	try {
    		this.conn = dataSource.getConnection();
    		
    		String sql = "insert into LINK_DOC (USER_ID, NAME_DOC, LINK_DOC, DOC_ID, ID_PROJECT, D_T_CREATE, SHORT_TEXT, THEME_ID, TYPE_ID)" + 
    					 "values(?,?,?,?,?,?,?, 999, 999)";
    		
    		try (PreparedStatement ps = conn.prepareStatement(sql);) {
    			ps.setInt(1, userService.getUser().getUserId());
    			ps.setString(2, event.getFile().getFileName());
    			ps.setString(3, path);
    			ps.setString(4, docId);
    			ps.setInt(5, idProject);
    			ps.setTimestamp(6, dt);
    			ps.setString(7, "");
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
					
					//FacesContext.getCurrentInstance().getExternalContext().redirect("/spd_web/pages/index.xhtml");
					userService.openMainDoc();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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

	public int getIdProject() {
		return idProject;
	}

	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}


}
