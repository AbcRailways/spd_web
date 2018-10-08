package kz.railways.entities;

import java.io.Serializable;

public class UserProject implements Serializable {

	private static final long serialVersionUID = 1L;

	private int idProject;
	private String project;
	private String mnkdProj;
	
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
	
}
