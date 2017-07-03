package com.harmonycloud.dto.external;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.BaseResource;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint extends BaseResource{
	private List<Subsets> subsets;

    public List<Subsets> getSubsets() {
            return subsets;
    }

    public void setSubsets(List<Subsets> subsets) {
            this.subsets = subsets;
    }

    public Endpoint(){
            this.setApiVersion("");
            this.setKind("");
    }

}
