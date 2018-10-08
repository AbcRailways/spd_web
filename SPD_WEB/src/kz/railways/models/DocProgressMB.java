package kz.railways.models;

import org.primefaces.component.organigram.OrganigramHelper;
import org.primefaces.event.organigram.OrganigramNodeCollapseEvent;
import org.primefaces.event.organigram.OrganigramNodeDragDropEvent;
import org.primefaces.event.organigram.OrganigramNodeExpandEvent;
import org.primefaces.event.organigram.OrganigramNodeSelectEvent;
import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.OrganigramNode;

import models.UserMB;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.sql.DataSource;


@ManagedBean(name = "progressMB")
@ViewScoped
public class DocProgressMB implements Serializable {

	
	@ManagedProperty("#{userMB}")
	private UserMB userService;
	public UserMB getUserService() {
		return userService;
	}

	public void setUserService(UserMB userService) {
		this.userService = userService;
	}

	private static final long serialVersionUID = -6127021745888957710L;	
	@Resource(mappedName = "jdbc/DB2Dsapp")
    private DataSource dataSource;
	private Connection conn;
	private OrganigramNode rootNode;
    private OrganigramNode selection;
    private String style = "width: 100%";
    private int leafNodeConnectorHeight = 10;
    private boolean autoScrollToSelection = false;
    
    @PostConstruct
    public void init(){
    	viewDocProgress(userService.getSelectedDoc().getDocId());
    }
    
    public void viewDocProgress(String docId){
    	rootNode = new DefaultOrganigramNode("root", docId, null);
    	rootNode.setCollapsible(false);    
    	String doc_id = docId;
    	try {
			this.conn = dataSource.getConnection();
			PreparedStatement ps;
			String sql = "select USER_INFO.FULL_NAME||' '||USER_INFO.SURNAME as UNAME, SIGN_DOC.SIGN_DOC, SIGN_DOC.READ_DOC from SIGN_DOC, USER_INFO"
+" where DOC_ID=? and ACTION_TYPE=1 and SIGN_DOC.USER_ID_POL=USER_INFO.USER_ID order by npp_sogl";
			ps = conn.prepareStatement(sql, 
					  ResultSet.TYPE_SCROLL_INSENSITIVE, 
					  ResultSet.CONCUR_READ_ONLY);
			ps.setString(1, doc_id);
			ResultSet rs1 = ps.executeQuery();
			rs1.last();
		    int size = rs1.getRow();
		    System.out.println(rs1.getRow());
		    rs1.beforeFirst();
			String[] fuck = new String[size];
			System.out.println(size-1);
			while (rs1.next()) {			
			fuck[rs1.getRow()-1]= rs1.getString("UNAME")+"<br />"+(rs1.getInt("READ_DOC")==1 ? "<p style=\"color:#01531D\">Просмотрен</p>" : "<p style=\"color:#ffff00\">Не просмотрен</p>");			
				if (rs1.getInt("SIGN_DOC")==2){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#01531D\">Согласован</p>";						
				} else if (rs1.getInt("SIGN_DOC")==3){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#FF0000\">Отказ</p>";		
				} else if (rs1.getInt("SIGN_DOC")==0){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#ffff00\">Не согласован</p>";
				}
			}
			OrganigramNode sogl = addDivision(rootNode, "Согласование", fuck);
			rs1.close();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	    	
    	
    	try {
			this.conn = dataSource.getConnection();
			PreparedStatement ps;
			String sql = "select USER_INFO.FULL_NAME||' '||USER_INFO.SURNAME as UNAME, SIGN_DOC.SIGN_DOC, SIGN_DOC.READ_DOC from SIGN_DOC, USER_INFO"
+" where DOC_ID=? and ACTION_TYPE=2 and SIGN_DOC.USER_ID_POL=USER_INFO.USER_ID order by npp_sogl";
			ps = conn.prepareStatement(sql, 
					  ResultSet.TYPE_SCROLL_INSENSITIVE, 
					  ResultSet.CONCUR_READ_ONLY);
			ps.setString(1, doc_id);
			ResultSet rs1 = ps.executeQuery();
			rs1.last();
		    int size = rs1.getRow();
		    System.out.println(rs1.getRow());
		    rs1.beforeFirst();
			String[] fuck = new String[size];
			System.out.println(size-1);
			while (rs1.next()) {
			fuck[rs1.getRow()-1]= rs1.getString("UNAME")+"<br />"+(rs1.getInt("READ_DOC")==1 ? "<p style=\"color:#01531D\">Просмотрен</p>" : "<p style=\"color:#ffff00\">Не просмотрен</p>" );
				if (rs1.getInt("SIGN_DOC")==1){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#01531D\">Подписан</p>";						
				} else if (rs1.getInt("SIGN_DOC")==3){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#FF0000\">Отказ</p>";		
				} else if (rs1.getInt("SIGN_DOC")==0){
					fuck[rs1.getRow()-1]=fuck[rs1.getRow()-1]+"<p style=\"color:#ffff00\">Не подписан</p>";
				}
			}
			OrganigramNode podp = addDivision(rootNode, "Подписание", fuck);
			rs1.close();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
    	
    	try {
			this.conn = dataSource.getConnection();
			PreparedStatement ps;
			String sql = "select USER_INFO.FULL_NAME||' '||USER_INFO.SURNAME as UNAME, SIGN_DOC.SIGN_DOC, SIGN_DOC.READ_DOC from SIGN_DOC, USER_INFO"
+" where DOC_ID=? and ACTION_TYPE=0 and SIGN_DOC.USER_ID_POL=USER_INFO.USER_ID order by npp_sogl";
			ps = conn.prepareStatement(sql, 
					  ResultSet.TYPE_SCROLL_INSENSITIVE, 
					  ResultSet.CONCUR_READ_ONLY);
			ps.setString(1, doc_id);
			ResultSet rs1 = ps.executeQuery();
			rs1.last();
		    int size = rs1.getRow();
		    System.out.println(rs1.getRow());
		    rs1.beforeFirst();
			String[] fuck = new String[size];
			System.out.println(size-1);
			while (rs1.next()) {
			fuck[rs1.getRow()-1]= rs1.getString("UNAME")+"<br />"+(rs1.getInt("READ_DOC")==1 ? "<p style=\"color:#01531D\">Просмотрен</p>" : "<p style=\"color:#ffff00\">Не просмотрен</p>" );		
			}
			OrganigramNode podp = addDivision(rootNode, "Корреспондент", fuck);
			rs1.close();
			ps.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
    	
    }
    
    protected OrganigramNode addDivision(OrganigramNode parent, String name, String... employees) {
        OrganigramNode divisionNode = new DefaultOrganigramNode("division", name, parent);
        divisionNode.setDroppable(true);
        divisionNode.setDraggable(true);
        divisionNode.setSelectable(true);
 
        if (employees != null) {
            for (String employee : employees) {
                OrganigramNode employeeNode = new DefaultOrganigramNode("employee", employee, divisionNode);
                employeeNode.setDraggable(true);
                employeeNode.setSelectable(true);
            }
        }
 
        return divisionNode;
    }
    
    public OrganigramNode getRootNode() {
        return rootNode;
    }
 
    public void setRootNode(OrganigramNode rootNode) {
        this.rootNode = rootNode;
    }
 
    public OrganigramNode getSelection() {
        return selection;
    }
 
    public void setSelection(OrganigramNode selection) {
        this.selection = selection;
    }
    
    public int getLeafNodeConnectorHeight() {
        return leafNodeConnectorHeight;
    }
 
    public void setLeafNodeConnectorHeight(int leafNodeConnectorHeight) {
        this.leafNodeConnectorHeight = leafNodeConnectorHeight;
    }
    
    public boolean isAutoScrollToSelection() {
        return autoScrollToSelection;
    }
 
    public void setAutoScrollToSelection(boolean autoScrollToSelection) {
        this.autoScrollToSelection = autoScrollToSelection;
    }
    
    public String getStyle() {
        return style;
    }
 
    public void setStyle(String style) {
        this.style = style;
    }
 
	
}
