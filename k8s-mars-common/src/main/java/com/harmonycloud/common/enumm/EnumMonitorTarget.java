package com.harmonycloud.common.enumm;


/**
 * target
 *
 * @author jmi
 */
public enum EnumMonitorTarget {

    CPU("cpu/usage_rate"),
    MEMORY("memory/usage"),
    DISK("filesystem/usage"),
    RX("network/rx_rate"),
    TX("network/tx_rate"),
    NODECPU("cpu/node_utilization"),
    PROCESSCPU("process/cpu_usage_rate"),
    PROCESSMEM("process/memory_usage_rate"),
    VOLUME("volume/usage");

    private String target;

    private EnumMonitorTarget(String target) {
        this.target = target;
    }

    public static EnumMonitorTarget getTargetData(String type) {
        for (EnumMonitorTarget mTarget : EnumMonitorTarget.values()) {
            if (mTarget.name().equals(type)) {
                return mTarget;
            }
        }
        return null;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


}
