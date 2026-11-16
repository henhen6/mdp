package top.mddata.workbench.service;

import top.mddata.base.base.R;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.vo.FingerprintCredentialVo;
import top.mddata.workbench.vo.FingerprintLoginOptionsVo;
import top.mddata.workbench.vo.FingerprintRegisterOptionsVo;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;

/**
 * 指纹登录服务
 *
 * @author tangyh
 * @since 2026/11/13
 */
public interface FingerprintService {

    /**
     * 录入前验证当前登录密码，通过后发一次性票据（5分钟）
     */
    R<Boolean> checkPassword(String password);

    /**
     * 获取录入挑战（消费一次性票据）
     */
    FingerprintRegisterOptionsVo registrationOptions();

    /**
     * 校验录入结果并保存凭证
     */
    FingerprintCredentialVo verifyRegistration(FingerprintRegisterVerifyDto dto);

    /**
     * 我的指纹凭证列表
     */
    List<FingerprintCredentialVo> listMine();

    /**
     * 修改设备名称
     */
    Boolean rename(Long id, FingerprintRenameDto dto);

    /**
     * 删除指纹凭证（逻辑删除）
     */
    Boolean delete(Long id);

    /**
     * 获取指纹登录挑战（免登录）
     */
    FingerprintLoginOptionsVo loginOptions();

    /**
     * 指纹登录验证（免登录，校验通过后签发 token）
     */
    R<LoginVo> loginVerify(FingerprintLoginVerifyDto dto);
}
