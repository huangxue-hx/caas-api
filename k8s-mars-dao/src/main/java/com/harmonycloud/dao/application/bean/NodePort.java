package com.harmonycloud.dao.application.bean;

public class NodePort {

	    private static final long serialVersionUID = 405310782098940013L;
	    //ID
	    private Integer id;
	       
	    public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getNodePort() {
			return nodePort;
		}

		public void setNodePort(Integer nodePort) {
			this.nodePort = nodePort;
		}

		public static long getSerialversionuid() {
			return serialVersionUID;
		}
		//随机生成nodeport端口
		private Integer nodePort; 
		

}
