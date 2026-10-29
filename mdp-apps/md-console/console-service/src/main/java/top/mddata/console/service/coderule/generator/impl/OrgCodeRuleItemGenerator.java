package top.mddata.console.service.coderule.generator.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.console.dto.coderule.CodeRuleItemConfig;
import top.mddata.console.enumeration.coderule.CodeRuleTypeEnum;
import top.mddata.console.enumeration.coderule.OrgCodeSourceEnum;
import top.mddata.console.service.coderule.generator.GenerateContext;
import top.mddata.console.service.coderule.generator.RuleItemGenerator;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.UserOrgRelService;

/**
 * 组织编码生成器：取当前上下文用户的部门/机构（公司）编码（mdc_org.code）。
 * 用户有多个组织关系时取第一个；机构编码取所属顶级公司（tree_path 第一段）。
 */
@Component
@RequiredArgsConstructor
public class OrgCodeRuleItemGenerator implements RuleItemGenerator {

    private final UserOrgRelService userOrgRelService;
    private final OrgService orgService;

    @Override
    public CodeRuleTypeEnum getType() {
        return CodeRuleTypeEnum.ORG_CODE;
    }

    @Override
    public String generate(CodeRuleItemConfig config, GenerateContext context) {
        ArgumentAssert.notNull(context.getUserId(), "组织编码规则需要登录上下文");
        UserOrgRel rel = userOrgRelService.getOne(QueryWrapper.create()
                .eq(UserOrgRel::getUserId, context.getUserId()).orderBy(UserOrgRel::getId, true).limit(1));
        ArgumentAssert.notNull(rel, "当前用户未分配组织，无法取组织编码");

        Org target;
        if (OrgCodeSourceEnum.COMPANY.getCode().equals(config.getSource())) {
            Org dept = orgService.getById(rel.getOrgId());
            ArgumentAssert.notNull(dept, "组织[{}]不存在", rel.getOrgId());
            // tree_path 形如 /顶级公司id/.../，第一段即顶级公司
            String treePath = dept.getTreePath();
            ArgumentAssert.notBlank(treePath, "组织[{}]未配置树路径", dept.getName());
            Long companyId = Long.valueOf(treePath.split("/")[1]);
            target = dept.getId().equals(companyId) ? dept : orgService.getById(companyId);
        } else {
            target = orgService.getById(rel.getOrgId());
        }
        ArgumentAssert.notNull(target, "组织不存在");
        ArgumentAssert.notBlank(target.getCode(), "组织[{}]未配置编码", target.getName());
        return target.getCode();
    }
}
