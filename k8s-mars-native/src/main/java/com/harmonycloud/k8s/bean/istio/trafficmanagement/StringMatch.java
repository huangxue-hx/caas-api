package com.harmonycloud.k8s.bean.istio.trafficmanagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Create by weg on 18-11-30.
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StringMatch {

    private String exact;

    private String prefix;

    private String regex;

    public String getExact() {
        return exact;
    }

    public void setExact(String exact) {
        this.exact = exact;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        StringMatch stringMatch = (StringMatch) o;
        if (this.exact != null) {
            if (!this.exact.equals(stringMatch.exact)) {
                return false;
            }
        } else {
            if (stringMatch.exact != null) {
                return false;
            }
        }
        if (this.prefix != null) {
            if (!this.prefix.equals(stringMatch.prefix)) {
                return false;
            }
        } else {
            if (stringMatch.prefix != null) {
                return false;
            }
        }
        if (this.regex != null) {
            if (!this.regex.equals(stringMatch.regex)) {
                return false;
            }
        } else {
            if (stringMatch.regex != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getExact() != null ? getExact().hashCode() : 0;
        result = 31 * result + (getPrefix() != null ? getPrefix().hashCode() : 0);
        result = 31 * result + (getRegex() != null ? getRegex().hashCode() : 0);
        return result;
    }
}
