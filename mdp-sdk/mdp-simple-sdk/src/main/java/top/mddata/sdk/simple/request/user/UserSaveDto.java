package top.mddata.sdk.simple.request.user;

import java.io.Serializable;


/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */

public class UserSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private String userSource;

    public String getUsername() {
        return username;
    }

    public UserSaveDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public UserSaveDto setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserSaveDto setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserSaveDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserSaveDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUserSource() {
        return userSource;
    }

    public UserSaveDto setUserSource(String userSource) {
        this.userSource = userSource;
        return this;
    }
}
