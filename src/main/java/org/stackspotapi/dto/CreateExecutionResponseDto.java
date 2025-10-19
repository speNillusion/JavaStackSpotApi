// No arquivo dto/CreateExecutionResponseDto.java
package org.stackspotapi.dto;

public class CreateExecutionResponseDto {
    private String executionId;

    public CreateExecutionResponseDto(String executionId) {
        this.executionId = executionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
}
