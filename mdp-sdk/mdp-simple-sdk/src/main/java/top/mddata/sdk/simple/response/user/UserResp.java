package top.mddata.sdk.simple.response.user;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户返回
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */

public class UserResp implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

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

    /**
     * 状态
     * [0-禁用 1-正常]
     */
    
    private Boolean state;


    /**
     * 创建时间
     */
    
    private LocalDateTime createdAt;


    private String userSource;

    public Long getId() {
        return id;
    }

    public UserResp setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserResp setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public UserResp setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserResp setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserResp setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserResp setEmail(String email) {
        this.email = email;
        return this;
    }

    public Boolean getState() {
        return state;
    }

    public UserResp setState(Boolean state) {
        this.state = state;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserResp setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getUserSource() {
        return userSource;
    }

    public UserResp setUserSource(String userSource) {
        this.userSource = userSource;
        return this;
    }
}
