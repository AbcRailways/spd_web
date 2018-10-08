package kz.railways.entities;

import java.io.Serializable;
import java.util.Date;

public class MainDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private int idProject;
	private String project;
	private String mnkdProj;
	private String nameDoc;
	private String linkDoc;
	private String docId;
	private Date dtCreate;
	
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
	public String getLinkDoc() {
		return linkDoc;
	}
	public void setLinkDoc(String linkDoc) {
		this.linkDoc = linkDoc;
	}
//	public int getDocId() {
//		return docId;
//	}
//	public void setDocId(int docId) {
//		this.docId = docId;
//	}
	public int getIdProject() {
		return idProject;
	}
	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}
	public String getNameDoc() {
		return nameDoc;
	}
	public void setNameDoc(String nameDoc) {
		this.nameDoc = nameDoc;
	}
	public Date getDtCreate() {
		return dtCreate;
	}
	public void setDtCreate(Date dtCreate) {
		this.dtCreate = dtCreate;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
}
