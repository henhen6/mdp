package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baidu.fsg.uid.UidGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.facade.FileFacade;
import top.mddata.open.admin.dto.AppApplyReviewDto;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.entity.AppApply;
import top.mddata.open.admin.mapper.AppApplyMapper;
import top.mddata.open.admin.service.AppApplyService;
import top.mddata.open.admin.service.AppService;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 应用申请 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppApplyServiceImpl extends SuperServiceImpl<AppApplyMapper, AppApply> implements AppApplyService {
    private final AppService appService;
    private final FileFacade fileFacade;
    private final UidGenerator uidGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(AppApplyReviewDto dto) {
        AppApply applicationApply = this.getById(dto.getId());
        ArgumentAssert.notNull(applicationApply, "待审核信息不存在");
        ArgumentAssert.isTrue(AuditStatusEnum.PENDING.eq(applicationApply.getAuditStatus()) ||
                              AuditStatusEnum.REJECTED.eq(applicationApply.getAuditStatus()), "当前状态不允许审核");

        // 通过
        if (dto.getApprove()) {
            App app = BeanUtil.toBean(applicationApply, App.class);
            app.setId(uidGenerator.getUid());
            // 复制头像
            CopyFilesDto copyFilesDto = CopyFilesDto.builder()
                    .objectType(FileObjectType.Open.APP_APPLY_LOGO).objectId(applicationApply.getId())
                    .build();
            copyFilesDto.addTargetFiles(FileObjectType.Open.APP_LOGO, app.getId());
            Boolean copyFlag = fileFacade.copyFile(copyFilesDto);

            // 新建应用
            app.setApplyId(applicationApply.getId());
            String appKey = new SimpleDateFormat("yyyyMMdd").format(new Date()) + RandomUtil.randomString(10);
            app.setAppKey(appKey);
            if (copyFlag) {
                app.setLogo(app.getId());
            }
            app.setAppSecret(RandomUtil.randomString(36));
            appService.save(app);

            // 修改审核状态
            AppApply apply = new AppApply();
            apply.setId(dto.getId());
            apply.setAuditStatus(AuditStatusEnum.APPROVED.getCode());
            apply.setReviewComments(dto.getReviewComments());
            apply.setAuditAt(LocalDateTime.now());
            updateById(apply);
        } else {
            AppApply apply = new AppApply();
            apply.setId(dto.getId());
            apply.setAuditStatus(AuditStatusEnum.REJECTED.getCode());
            apply.setReviewComments(dto.getReviewComments());
            apply.setAuditAt(LocalDateTime.now());
            updateById(apply);
        }
        return true;
    }
}
