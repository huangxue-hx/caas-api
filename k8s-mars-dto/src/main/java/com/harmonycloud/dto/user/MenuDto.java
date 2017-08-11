package com.harmonycloud.dto.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zsl on 16/10/25.
 */
public class MenuDto implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 9177592241727889838L;
	private Integer id;
    private String name;
    private String transName;
    private String iconName;
    private String url;

    private List<MenuDto> subMenu;

    public MenuDto(){}

    public MenuDto(Integer id, String name, String transName, String iconName, String url){
        this.id = id;
        this.name = name;
        this.transName = transName;
        this.iconName = iconName;
        this.url = url;
    }



    /**
     * 用递归完成菜单树状结构
     */
    private static void addSubMenu(List<com.harmonycloud.dao.user.bean.Resource> resources, MenuDto menuVo){
        for(com.harmonycloud.dao.user.bean.Resource resource : resources){
            if (resource.getParentId().longValue() == menuVo.getId().longValue()){
                MenuDto tmp = new MenuDto(resource.getId(), resource.getName(), resource.getTransName(), resource.getIconName(), resource.getUrl());
                if (menuVo.getSubMenu() == null){
                    menuVo.setSubMenu(new ArrayList<MenuDto>());
                }
                menuVo.getSubMenu().add(tmp);
                addSubMenu(resources,tmp);
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<MenuDto> getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(List<MenuDto> subMenu) {
        this.subMenu = subMenu;
    }

    public String toString() {
        return "MenuDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", transName='" + transName + '\'' +
                ", iconName='" + iconName + '\'' +
                ", url='" + url + '\'' +
                ", subMenu=" + subMenu +
                '}';
    }
}
