package top.mddata.console.service.organization;

import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.enumeration.organization.UserIdentityEnum;

import java.util.List;

/**
 * 组织可见性 服务层。
 *
 * <p>可见性规则见 docs/用户体系/用户角色组织权限体系设计.md 第 7 节：
 * 运营者可见全部；开发者管理员仅开发者平台子树；普通用户仅总公司子树；开发者不可见。</p>
 */
public interface OrgVisibilityService {

    /**
     * 当前操作人身份
     *
     * @return 用户身份
     */
    UserIdentityEnum currentIdentity();

    /**
     * 当前操作人可见的组织树根节点id列表。
     *
     * @return null=不限制（运营者）；空列表=什么都不可见（开发者）；其余=可见根节点
     */
    List<Long> currentVisibleRootOrgIds();

    /**
     * 身份到可见根节点的映射（纯函数）
     *
     * @param identity 用户身份
     * @return null=不限制；空列表=不可见；其余=可见根节点
     */
    static List<Long> visibleRootOrgIds(UserIdentityEnum identity) {
        if (identity == null) {
            return List.of();
        }
        return switch (identity) {
            case OPERATIONS_ADMIN -> null;
            case DEVELOPER_ADMIN -> List.of(BuiltInOrgId.DEVELOPER_PLATFORM);
            case USER -> List.of(BuiltInOrgId.HEAD_COMPANY);
            case DEVELOPER -> List.of();
        };
    }

    /**
     * 追加用户可见性过滤：只显示可见组织树内的用户。
     *
     * @param wrapper    查询条件
     * @param rootIds    可见组织树根节点；null=不限制（运营者）；空列表=只看自己（兜底）
     * @param selfUserId 当前操作人id（rootIds 为空时兜底用）
     */
    static void appendUserVisibilityFilter(QueryWrapper wrapper, List<Long> rootIds,
                                           Long selfUserId) {
        if (rootIds == null) {
            return;
        }
        if (rootIds.isEmpty()) {
            wrapper.eq(User::getId, selfUserId);
            return;
        }
        wrapper.and(qw -> {
            for (Long rootId : rootIds) {
                QueryWrapper sub = QueryWrapper.create().select("1").from(UserOrgRel.class)
                        .innerJoin(Org.class).on(UserOrgRel::getOrgId, Org::getId)
                        .where(UserOrgRel::getUserId).eq(User::getId)
                        .and(Org::getTreePath).like("/" + rootId + "/");
                qw.or(QueryMethods.exists(sub));
            }
        });
    }
}
