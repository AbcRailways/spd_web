package kz.railways.entities;

import java.io.Serializable;
import java.util.Date;

public class SendOrIncoming1 implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int npp;
	private int userIdOtpr;
	private int userIdPol;
	private String surname;
	private String fullName;
	private int idProject;
	private String project;
	private String mnkdProj;
	private String nameDoc;
	private String linkDoc;
	private String docId;
	private Date dtSend;
	private Date dtCreate;
	private int readDoc;
	private int actionType;
	
	public int getUserIdOtpr() {
		return userIdOtpr;
	}
	public void setUserIdOtpr(int userIdOtpr) {
		this.userIdOtpr = userIdOtpr;
	}
	public int getUserIdPol() {
		return userIdPol;
	}
	public void setUserIdPol(int userIdPol) {
		this.userIdPol = userIdPol;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public int getIdProject() {
		return idProject;
	}
	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getMnkdProj() {
		return mnkdProj;
	}
	public void setMnkdProj(String mnkdProj) {
		this.mnkdProj = mnkdProj;
	}
	public String getNameDoc() {
		return nameDoc;
	}
	public void setNameDoc(String nameDoc) {
		this.nameDoc = nameDoc;
	}
	public String getLinkDoc() {
		return linkDoc;
	}
	public void setLinkDoc(String linkDoc) {
		this.linkDoc = linkDoc;
	}
	/*public int getDocId() {
		return docId;
	}
	public void setDocId(int docId) {
		this.docId = docId;
	}*/
	public Date getDtSend() {
		return dtSend;
	}
	public void setDtSend(Date dtSend) {
		this.dtSend = dtSend;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public Date getDtCreate() {
		return dtCreate;
	}
	public void setDtCreate(Date dtCreate) {
		this.dtCreate = dtCreate;
	}
	public int getReadDoc() {
		return readDoc;
	}
	public void setReadDoc(int readDoc) {
		this.readDoc = readDoc;
	}
	public int getNpp() {
		return npp;
	}
	public void setNpp(int npp) {
		this.npp = npp;
	}
	public int getActionType() {
		return actionType;
	}
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}
	
}
