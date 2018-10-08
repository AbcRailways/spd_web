package models;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.sql.DataSource;

@ManagedBean(name = "messageMB")
@SessionScoped
public class MessageMB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	
	private Connection conn;
	
	//update поля READ_DOC при просмотре документа
    /*public  void updateReadDoc() {
    	try {
    		conn = dataSource.getConnection();
    		
    		String sql = "update SIGN_DOC set READ_DOC = ? where USER_ID_OTPR = ? and USER_ID_POL = ? and DOC_ID = ?";
    		
    		try (PreparedStatement ps = conn.prepareStatement(sql);) {
    			ps.setShort(1, readDoc);
    			ps.setInt(2, userIdOtpr);
    			ps.setInt(3, userIdPol);
    			ps.setString(4, docId);
    			ps.executeUpdate();
    		}
    		
    	} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw (EJBException) new EJBException(e).initCause(e); 
		}
		finally {
			try {
				conn.close();
			} catch (SQLException fse) 
			{
				fse.printStackTrace();
			}
		}
    }*/
    
    
}
