package top.mddata.gateway.sop.service.dubbo;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.sop.support.enums.RegSourceEnum;
import com.gitee.sop.support.service.ApiRegisterService;
import com.gitee.sop.support.service.dto.RegisterDTO;
import com.gitee.sop.support.service.dto.RegisterResult;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import top.mddata.common.enumeration.StateEnum;
import top.mddata.gateway.sop.manager.ApiManager;
import top.mddata.open.admin.entity.Api;
import top.mddata.open.admin.mapper.ApiMapper;

import java.util.Collection;
import java.util.Objects;

/**
 * @author 六如
 */
@Slf4j
@DubboService
public class ApiRegisterServiceImpl implements ApiRegisterService {

    @Resource
    private ApiManager apiManager;
    @Resource
    private ApiMapper apiMapper;

    @Override
    public RegisterResult register(Collection<RegisterDTO> registerDTOS) {
        try {
            for (RegisterDTO registerDTO : registerDTOS) {
                log.info("注册开放接口, registerDTO={}", registerDTO);
                this.doReg(registerDTO);
            }
            return RegisterResult.success();
        } catch (Exception e) {
            log.error("接口注册失败", e);
            return RegisterResult.error(e.getMessage());
        }
    }

    private void doReg(RegisterDTO registerDTO) {
        Api apiInfo = apiMapper.selectOneByQuery(QueryWrapper.create().eq(Api::getMethodName, registerDTO.getMethodName())
                .eq(Api::getApiVersion, registerDTO.getApiVersion()));
        if (apiInfo == null) {
            apiInfo = new Api();
        } else {
            check(apiInfo, registerDTO);
        }
        BeanUtil.copyProperties(registerDTO, apiInfo);
        apiInfo.setRegSource(RegSourceEnum.SYSTEM.getCode());
        apiInfo.setState(StateEnum.ENABLE.getInteger());
        // 保存到数据库
        apiManager.saveOrUpdate(apiInfo);
    }

    private void check(Api apiInfo, RegisterDTO registerDTO) {
        if (!Objects.equals(apiInfo.getAppName(), registerDTO.getAppName())) {
            throw new RuntimeException("接口[" + registerDTO + "]已存在于[" + apiInfo.getAppName() + "]应用中.必须保证接口全局唯一");
        }
    }

}
