package com.harmonycloud.dto.cluster;
import java.util.List;

/**
 * @author youpeiyuan
 *
 */
public class TransferResultDto {

    private List<ErrDeployDto> errDeployDtos;

    private List<ErrorNamespaceDto> errNamespaceDtos;

    private boolean status;

    public List<ErrDeployDto> getErrDeployDtos() {
        return errDeployDtos;
    }

    public void setErrDeployDtos(List<ErrDeployDto> errDeployDtos) {
        this.errDeployDtos = errDeployDtos;
    }

    public List<ErrorNamespaceDto> getErrNamespaceDtos() {
        return errNamespaceDtos;
    }

    public void setErrNamespaceDtos(List<ErrorNamespaceDto> errNamespaceDtos) {
        this.errNamespaceDtos = errNamespaceDtos;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TransferResultDto [errDeployDtos=");
        builder.append(errDeployDtos);
        builder.append(", errNamespaceDtos=");
        builder.append(errNamespaceDtos);
        builder.append(", status=");
        builder.append(status);
        builder.append("]");
        return builder.toString();
    }


}