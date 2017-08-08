package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborUserBinding;
import com.harmonycloud.service.platform.bean.ProjectUserBinding;
import com.harmonycloud.service.platform.bean.UserProjectBiding;

public interface HarborProjectService {
	
	public ActionReturnUtil getAllImageOfUser(String namespace,String username) throws Exception;
	
	public ActionReturnUtil listHarborRole() throws Exception;
	
	public ActionReturnUtil bindingHarborUser(HarborUserBinding harborUserBinding) throws Exception;
	
	public ActionReturnUtil deleteHarborUser(HarborUserBinding harborUserBinding) throws Exception;
	
	public ActionReturnUtil getStatistcsByNamespace(String namespace) throws Exception;

	public ActionReturnUtil bindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception;
	
	public ActionReturnUtil unBindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception;

	public ActionReturnUtil bindingProjectUsers(UserProjectBiding userProjectBinding)throws Exception;

}
