package com.harmonycloud.k8s.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @param <T> 泛型参数： maxUnavailable与minAvailable均可设置为整数（Integer）或百分数（String）
 *           maxUnavailable与minAvailable二选一。
 *           示例：1（Integer），50%（String）
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodDisruptionBudgetSpec <T> {
    private T maxUnavailable;

    private T minAvailable;

    private LabelSelector selector;


    public T getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(T maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }

    public T getMinAvailable() {
        return minAvailable;
    }

    public void setMinAvailable(T minAvailable) {
        this.minAvailable = minAvailable;
    }

    public LabelSelector getSelector() {
        return selector;
    }

    public void setSelector(LabelSelector selector) {
        this.selector = selector;
    }
}
