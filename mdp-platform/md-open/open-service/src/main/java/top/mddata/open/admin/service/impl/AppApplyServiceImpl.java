package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.open.AppByAppKeyCkBuilder;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.dto.IdDto;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.facade.FileFacade;
import top.mddata.open.admin.dto.AppApplyDto;
import top.mddata.open.admin.dto.AppApplyReviewDto;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.entity.AppApply;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.mapper.AppApplyMapper;
import top.mddata.open.admin.service.AppApplyService;
import top.mddata.open.admin.service.AppKeysService;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.convert.AppApplyConvert;

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
    private final AppKeysService appKeysService;
    private final FileFacade fileFacade;
    private final UidGenerator uidGenerator;
    private final AppApplyConvert appApplyConvert;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(AppApplyDto dto) {
        AppApply entity = null;
        boolean isSave = false;
        if (dto.getId() != null) {
            entity = UpdateEntity.of(getEntityClass());
            appApplyConvert.copyTargetProperties(dto, entity);
        } else {
            entity = appApplyConvert.toSource(dto);
            entity.setId(uidGenerator.getUid());
            isSave = true;
        }
        entity.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        entity.setSubmissionAt(LocalDateTime.now());
        // 这里可以直接用当前数据ID，也可以重新生成一个唯一id
        entity.setLogo(entity.getId());
        entity.setCredentialFile(entity.getId());

        if (isSave) {
            save(entity);
        } else {
            updateById(entity);
        }

        // 关联附件
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getLogo())
                .objectType(FileObjectType.Open.APP_APPLY_LOGO)
                .build().setKeepFileIds(dto.getLogo()));

        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getCredentialFile())
                .objectType(FileObjectType.Open.APP_APPLY_CREDENTIAL_FILE)
                .build().addKeepFileIds(dto.getCredentialFile()));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long withdraw(IdDto dto) {
        AppApply appApply = new AppApply();
        appApply.setId(dto.getId());
        appApply.setAuditStatus(AuditStatusEnum.INITIALIZED.getCode());
        updateById(appApply);
        return dto.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(AppApplyReviewDto dto) {
        AppApply applicationApply = this.getById(dto.getId());
        ArgumentAssert.notNull(applicationApply, "待审核信息不存在");
        ArgumentAssert.isTrue(AuditStatusEnum.PENDING.eq(applicationApply.getAuditStatus()) ||
                              AuditStatusEnum.REJECTED.eq(applicationApply.getAuditStatus()), "当前状态不允许审核");

        // 通过
        if (dto.getApprove()) {
            long count = appService.count(QueryWrapper.create().eq(App::getApplyId, dto.getId()));
            ArgumentAssert.isTrue(count <= 0, "申请已审核通过，请勿重复审核");

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

            AppKeys appKeys = new AppKeys();
            appKeys.setAppId(app.getId());
            appKeys.setKeyFormat(RsaTool.KeyFormat.PKCS8.getCode());
            appKeysService.save(appKeys);
            cacheOps.del(AppByAppKeyCkBuilder.builder(app.getAppKey()));

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


    @Override
    protected AppApply saveBefore(Object save) {
        AppApply entity = appApplyConvert.toSource((AppApplyDto) save);
        entity.setId(uidGenerator.getUid());
        entity.setAuditStatus(AuditStatusEnum.INITIALIZED.getCode());

        // 这里可以直接用当前数据ID，也可以重新生成一个唯一id
        entity.setLogo(entity.getId());
        entity.setCredentialFile(entity.getId());
        return entity;
    }

    @Override
    protected void saveAfter(Object save, AppApply entity) {
        AppApplyDto dto = (AppApplyDto) save;

        // 关联附件
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getLogo())
                .objectType(FileObjectType.Open.APP_APPLY_LOGO)
                .build()
                .setKeepFileIds(dto.getLogo()));
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getCredentialFile())
                .objectType(FileObjectType.Open.APP_APPLY_CREDENTIAL_FILE)
                .build().addKeepFileIds(dto.getCredentialFile()));
    }

    @Override
    protected AppApply updateBefore(Object updateDto) {
        AppApply entity = UpdateEntity.of(getEntityClass());
        appApplyConvert.copyTargetProperties((AppApplyDto) updateDto, entity);
        entity.setAuditStatus(AuditStatusEnum.INITIALIZED.getCode());

        // 这里可以直接用当前数据ID，也可以重新生成一个唯一id
        entity.setLogo(entity.getId());
        entity.setCredentialFile(entity.getId());
        return entity;
    }

    @Override
    protected void updateAfter(Object updateDto, AppApply entity) {
        AppApplyDto dto = (AppApplyDto) updateDto;
        // 关联附件 注意：dto.logo 是前端传递过来的文件id， entity.logo 是在存入数据库前，设置的唯一对象id（为了节约雪花id，可以复用entity.getId(), 即可生成新的唯一id）
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getLogo())
                .objectType(FileObjectType.Open.APP_APPLY_LOGO)
                .build()
                .setKeepFileIds(dto.getLogo())
        );

        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getCredentialFile())
                .objectType(FileObjectType.Open.APP_APPLY_CREDENTIAL_FILE)
                .build().addKeepFileIds(dto.getCredentialFile()));
    }
}
