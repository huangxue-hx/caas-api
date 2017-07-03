package com.harmonycloud.dto.tenant.show;

import java.util.List;

/**
 * Created by andy on 17-2-7.
 */
public class QuotaDetailShowDto {

    private List<String> cpu;

    private List<String> memory;
    
    public List<String> getCpu() {
        return cpu;
    }

    public void setCpu(List<String> cpu) {
        this.cpu = cpu;
    }

    public List<String> getMemory() {
        return memory;
    }

    public void setMemory(List<String> memory) {
        this.memory = memory;
    }
}
