package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.vo.admin.OauthUnionidVo;

/**
 * Unionid
 *
 * @author henhen6
 * @since 2025/8/21 23:33
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/inner/oauthUnionid")
public interface OauthUnionidApi {
    /**
     * 根据主体id和用户id 查询union
     *
     * @param subjectId 主体id
     * @param userId    用户id
     * @return union
     */
    @GetMapping("/getBySubjectIdAndUserId")
    R<OauthUnionidVo> getBySubjectIdAndUserId(@RequestParam Long subjectId, @RequestParam Long userId);
}
