package top.mddata.sdk.simple.request;


public class SaveBaseEmployeeRequest {

    private Integer positionStatus;
    private String name;
    private Integer activeStatus;

    public Integer getPositionStatus() {
        return positionStatus;
    }

    public SaveBaseEmployeeRequest setPositionStatus(Integer positionStatus) {
        this.positionStatus = positionStatus;
        return this;
    }

    public String getName() {
        return name;
    }

    public SaveBaseEmployeeRequest setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getActiveStatus() {
        return activeStatus;
    }

    public SaveBaseEmployeeRequest setActiveStatus(Integer activeStatus) {
        this.activeStatus = activeStatus;
        return this;
    }
}
