package com.harmonycloud.k8s.bean;

public class Resource {
	private String name;
	private String namespaced;
	private String kind;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespaced() {
		return namespaced;
	}
	public void setNamespaced(String namespaced) {
		this.namespaced = namespaced;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((namespaced == null) ? 0 : namespaced.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Resource other = (Resource) obj;
		if(this.name.equals(other.name)){
			return true;
		}
		return false;
	}
	
	
}
