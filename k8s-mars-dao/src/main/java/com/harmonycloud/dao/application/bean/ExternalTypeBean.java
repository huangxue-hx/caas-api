package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

/**
 * Created by ly on 17/3/30.
 * 外部服务bean
 */

public class ExternalTypeBean  implements Serializable{

    private static final long serialVersionUID = 405310782098940013L;
        //外部服务ID
        private Integer id;
       
		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public static long getSerialversionuid() {
			return serialVersionUID;
		}

		//外部服务名称  非空 唯一 
		private String type;
		
	
}
