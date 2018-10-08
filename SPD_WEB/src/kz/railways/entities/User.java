package kz.railways.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	private int userId;
	private String userName;
	private String surname;
	private String fullName;
	private String position;
	private String department;
	private String nPhone;
	private String signAlias;
	private String passKey;
	private String email;
	private List<UserProject> listUserProject;
	private List<LinkDoc> listLinkDoc;
	private List<LinkDoc> mainListDoc;
	private List<LinkDoc> listSend;
	private List<LinkDoc> listIncoming = new ArrayList<LinkDoc>();
	private List<LinkDoc> listOtkaz = new ArrayList<LinkDoc>();
	private List<LinkDoc> listOther;
	private String sign;
	private int pic;
	private boolean sogl;
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getnPhone() {
		return nPhone;
	}
	public void setnPhone(String nPhone) {
		this.nPhone = nPhone;
	}
	public String getSignAlias() {
		return signAlias;
	}
	public void setSignAlias(String signAlias) {
		this.signAlias = signAlias;
	}
	public String getPassKey() {
		return passKey;
	}
	public void setPassKey(String passKey) {
		this.passKey = passKey;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<UserProject> getListUserProject() {
		return listUserProject;
	}
	public void setListUserProject(List<UserProject> listUserProject) {
		this.listUserProject = listUserProject;
	}
	public List<LinkDoc> getListLinkDoc() {
		return listLinkDoc;
	}
	public void setListLinkDoc(List<LinkDoc> listLinkDoc) {
		this.listLinkDoc = listLinkDoc;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
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
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}	
	public List<LinkDoc> getMainListDoc() {
		return mainListDoc;
	}
	public void setMainListDoc(List<LinkDoc> mainListDoc) {
		this.mainListDoc = mainListDoc;
	}
	public int getPic() {
		return pic;
	}
	public void setPic(int pic) {
		this.pic = pic;
	}
	public List<LinkDoc> getListSend() {
		return listSend;
	}
	public void setListSend(List<LinkDoc> listSend) {
		this.listSend = listSend;
	}
	public List<LinkDoc> getListIncoming() {
		return listIncoming;
	}
	public void setListIncoming(List<LinkDoc> listIncoming) {
		this.listIncoming = listIncoming;
	}
	public boolean isSogl() {
		return sogl;
	}
	public void setSogl(boolean sogl) {
		this.sogl = sogl;
	}
	public List<LinkDoc> getListOtkaz() {
		return listOtkaz;
	}
	public void setListOtkaz(List<LinkDoc> listOtkaz) {
		this.listOtkaz = listOtkaz;
	}
	public List<LinkDoc> getListOther() {
		return listOther;
	}
	public void setListOther(List<LinkDoc> listOther) {
		this.listOther = listOther;
	}
		
}
