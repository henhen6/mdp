package top.mddata.sdk.simple.request.user;

/**
 * 获取用户参数
 * @author henhen
 */
public class UserGetByIdDto {

    private Long id;

    public Long getId() {
        return id;
    }

    public UserGetByIdDto setId(Long id) {
        this.id = id;
        return this;
    }
}
