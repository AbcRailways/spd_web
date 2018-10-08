package kz.railways.entities;

import java.io.Serializable;
import java.sql.Timestamp;


public class NewDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nameDoc;
	private String regNumber;
	private Timestamp dateDoc;
	private String textDoc;
	private String path;
	private String shortText;
	private int idProject;
	
	
	public String getRegNumber() {
		return regNumber;
	}
	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}
	public String getTextDoc() {
		return textDoc;
	}
	public void setTextDoc(String textDoc) {
		this.textDoc = textDoc;
	}
	public String getNameDoc() {
		return nameDoc;
	}
	public void setNameDoc(String nameDoc) {
		this.nameDoc = nameDoc;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getShortText() {
		return shortText;
	}
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}
	public int getIdProject() {
		return idProject;
	}
	public void setIdProject(int idProject) {
		this.idProject = idProject;
	}
	public Timestamp getDateDoc() {
		return dateDoc;
	}
	public void setDateDoc(Timestamp dateDoc) {
		this.dateDoc = dateDoc;
	}
	
}
