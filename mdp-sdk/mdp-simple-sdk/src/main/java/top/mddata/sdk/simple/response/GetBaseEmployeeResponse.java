package top.mddata.sdk.simple.response;


import java.time.LocalDateTime;

public class GetBaseEmployeeResponse {
    private Long id;
    private String realName;
    private LocalDateTime createdTime;

    public Long getId() {
        return id;
    }

    public GetBaseEmployeeResponse setId(Long id) {
        this.id = id;
        return this;
    }

    public String getRealName() {
        return realName;
    }

    public GetBaseEmployeeResponse setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public GetBaseEmployeeResponse setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
        return this;
    }
}
