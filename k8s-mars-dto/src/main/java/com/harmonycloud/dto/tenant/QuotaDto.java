package com.harmonycloud.dto.tenant;

/**
 * Created by andy on 17-1-20.
 */
public class QuotaDto {
    //cpu
    private String cpu;
    //内存
    private String memory;

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

}
