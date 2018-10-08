package kz.railways.entities;

import java.io.Serializable;
import java.util.Date;

public class LinkDoc implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int regNumber;		
	private String nameDoc;
	private String linkDoc;
	private String docId;
	private int idProject;
	private Date dtCreate;
	private Date dtRealize;
	private String shortText;
	private int themeId;
	private int typeId;
	private String answerIncom;
	
	/*************** поля необходимые для отображения входящих и отправленных ****************/
	private int nppSogl;
	private int userIdOtpr;
	private int userIdPol;
	private int signDoc;
	/*private int userIdSogl;
	private int userIdPodpis;
	private int userIdPoluch;*/
	/*private int status;
	private int priznak;*/
	private int readDoc;
	private Date dtSend;
	private int actionType;
	private String surname;
	private String fullName;
	private String position;
	private String department;
	private String nPhone;
	/*private Date datetime;*/
	private String project;
	private String mnkdProj;
	private String theme;
	private String type;
	private int nextUser;
	/*****************************************************************************************/
	
	private String textDoc;
	
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
	public String getShortText() {
		return shortText;
	}
	public void setShortText(String shortText) {
		this.shortText = shortText;
	}
	
	public int getRegNumber() {
		return regNumber;
	}
	public void setRegNumber(int regNumber) {
		this.regNumber = regNumber;
	}
	public String getTextDoc() {
		return textDoc;
	}
	public void setTextDoc(String textDoc) {
		this.textDoc = textDoc;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public Date getDtRealize() {
		return dtRealize;
	}
	public void setDtRealize(Date dtRealize) {
		this.dtRealize = dtRealize;
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
	public String getAnswerIncom() {
		return answerIncom;
	}
	public void setAnswerIncom(String answerIncom) {
		this.answerIncom = answerIncom;
	}
	public int getUserIdOtpr() {
		return userIdOtpr;
	}
	public void setUserIdOtpr(int userIdOtpr) {
		this.userIdOtpr = userIdOtpr;
	}
/*	public int getUserIdSogl() {
		return userIdSogl;
	}
	public void setUserIdSogl(int userIdSogl) {
		this.userIdSogl = userIdSogl;
	}
	public int getUserIdPodpis() {
		return userIdPodpis;
	}
	public void setUserIdPodpis(int userIdPodpis) {
		this.userIdPodpis = userIdPodpis;
	}
	public int getUserIdPoluch() {
		return userIdPoluch;
	}
	public void setUserIdPoluch(int userIdPoluch) {
		this.userIdPoluch = userIdPoluch;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getPriznak() {
		return priznak;
	}
	public void setPriznak(int priznak) {
		this.priznak = priznak;
	}*/
	public int getReadDoc() {
		return readDoc;
	}
	public void setReadDoc(int readDoc) {
		this.readDoc = readDoc;
	}
	/*public Date getDatetime() {
		return datetime;
	}
	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}*/
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
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getNppSogl() {
		return nppSogl;
	}
	public void setNppSogl(int nppSogl) {
		this.nppSogl = nppSogl;
	}
	public int getUserIdPol() {
		return userIdPol;
	}
	public void setUserIdPol(int userIdPol) {
		this.userIdPol = userIdPol;
	}
	public Date getDtSend() {
		return dtSend;
	}
	public void setDtSend(Date dtSend) {
		this.dtSend = dtSend;
	}
	public int getActionType() {
		return actionType;
	}
	public void setActionType(int actionType) {
		this.actionType = actionType;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public int getSignDoc() {
		return signDoc;
	}
	public void setSignDoc(int signDoc) {
		this.signDoc = signDoc;
	}
	public int getNextUser() {
		return nextUser;
	}
	public void setNextUser(int nextUser) {
		this.nextUser = nextUser;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getnPhone() {
		return nPhone;
	}
	public void setnPhone(String nPhone) {
		this.nPhone = nPhone;
	}
		
}
