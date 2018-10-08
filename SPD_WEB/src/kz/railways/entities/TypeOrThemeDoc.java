package kz.railways.entities;

import java.io.Serializable;

public class TypeOrThemeDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String typeOrThemeDoc;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTypeOrThemeDoc() {
		return typeOrThemeDoc;
	}
	public void setTypeOrThemeDoc(String typeOrThemeDoc) {
		this.typeOrThemeDoc = typeOrThemeDoc;
	}
}
