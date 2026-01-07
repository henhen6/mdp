package top.mddata.sdk.simple.response;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户返回
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */

public class UserVo implements Serializable {


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

    public UserVo setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserVo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public UserVo setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserVo setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserVo setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserVo setEmail(String email) {
        this.email = email;
        return this;
    }

    public Boolean getState() {
        return state;
    }

    public UserVo setState(Boolean state) {
        this.state = state;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserVo setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getUserSource() {
        return userSource;
    }

    public UserVo setUserSource(String userSource) {
        this.userSource = userSource;
        return this;
    }
}
