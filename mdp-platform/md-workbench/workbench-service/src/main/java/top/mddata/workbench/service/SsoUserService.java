package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.OrgNature;
import top.mddata.common.entity.User;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author henhen6
 * @since 2025-07-09 00:46:37
 */
public interface SsoUserService extends SuperService<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户
     */
    User getByUsername(String username);

    /**
     * 根据手机查询用户
     * @param phone 手机
     * @return 用户
     */
    User getByPhone(String phone);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户
     */
    User getByEmail(String email);

    /**
     * 重置密码错误次数
     *
     * @param id 用户id
     */
    void resetPwErrorNum(Long id);

    /**
     * 修改输错密码的次数
     *
     * @param id 用户Id
     */
    void incrPwErrorNumById(Long id);

    /**
     * 检测手机是否存在
     * @param phone 手机号
     * @param id 用户id
     * @return 存在=true
     */
    boolean checkPhone(String phone, Long id);

    /**
     * 检测邮箱是否存在
     * @param email 邮箱
     * @param id 用户id
     * @return 存在=true
     */
    boolean checkEmail(String email, Long id);

    /**
     * 根据邮箱注册账号
     * @param ssoUser 用户
     */
    void registerByEmail(User ssoUser);

    /**
     * 根据手机注册账号
     * @param ssoUser 用户
     */
    void registerByPhone(User ssoUser);

    /**
     * 根据用户id，查询指定公司{companyId}下的所有部门
     * @param userId 用户id
     * @param companyId 单位id
     * @return 部门列表
     */
    List<Org> findDeptByUserId(Long userId, Long companyId);

    /**
     *  查询 {orgList} 中id等于 {lastOrgId} 的公司 或 部门
     * @param orgList   公司 或 部门列表
     * @param lastOrgId 最后一次登录的公司ID 或 部门Id
     * @return 公司或部门
     */
    Org getDefaultOrg(List<Org> orgList, Long lastOrgId);

    /**
     * 根据部门id，递归查询部门的直属上级公司
     * @param deptId 部门id
     * @return 公司
     */
    Org getCompanyByDeptId(Long deptId);

    /**
     * 根据用户id，查询用户所属的所有公司
     * @param userId 用户id
     * @return 所有公司
     */
    List<Org> findCompanyByUserId(Long userId);

    /**
     * 根据id查询组织
     * @param id 组织id
     * @return 组织
     */
    Org getOrgByIdCache(Long id);

    /**
     * 判断用户所属的顶级公司是否是管理企业
     * @param id 单位id
     * @return 是否
     */
    boolean getTopCompanyIsAdminById(Long id);

    /**
     * 根据组织id查询默认的组织性质
     * @param id 组织id
     * @return
     */
    OrgNature getOrgNatureByOrgId(Long id);
}
