package top.mddata.sdk.simple.request.user;

/**
 * 分页查询用户信息 参数
 *
 * @author henhen
 */
public class UserQuery {


    /**
     * 用户名
     */
    private String username;

    /**
     * 性别
     * [1-男 2-女]
     */
    private String sex;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱地址
     */
    private String email;


    public String getUsername() {
        return username;
    }

    public UserQuery setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public UserQuery setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserQuery setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserQuery setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserQuery setEmail(String email) {
        this.email = email;
        return this;
    }
}
