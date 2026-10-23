package top.mddata.sdk.test.org;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.param.IdRequest;
import top.mddata.sdk.core.request.PageParams;
import top.mddata.sdk.core.response.Page;
import top.mddata.sdk.simple.api.org.OrgGetByIdApi;
import top.mddata.sdk.simple.api.org.OrgPageApi;
import top.mddata.sdk.simple.api.org.OrgSaveApi;
import top.mddata.sdk.simple.api.org.OrgUpdateByIdApi;
import top.mddata.sdk.simple.request.org.OrgQuery;
import top.mddata.sdk.simple.request.org.OrgSaveDto;
import top.mddata.sdk.simple.request.org.OrgUpdateDto;
import top.mddata.sdk.simple.response.org.OrgResp;
import top.mddata.sdk.test.BaseTest;

import java.util.Random;

/**
 *
 * @author henhen
 * @since 2026/1/8 23:45
 */
public class OrgTest extends BaseTest {

    @BeforeEach
    protected void setUp() throws Exception {
        // 获取 accessToken，用于需要认证的接口
        getAccessToken();
    }

    @Test
    public void testGetById() {
        OrgGetByIdApi api = new OrgGetByIdApi();
        api.setBizModel(new IdRequest().setId(100565911912586431L));
        Result<OrgResp> result = client.execute(api);
        logResult(result);
    }

    @Test
    public void testSave() {
        OrgSaveDto org1 = new OrgSaveDto();
        org1.setName("test" + new Random().nextInt(5000));
//        org1.setParentId(123L);
        org1.setOrgType("10");

        // 创建请求对象
        OrgSaveApi param = new OrgSaveApi();
        param.setBizModel(org1);

        // 发送请求
        Result<OrgResp> result = client.execute(param);

        logResult(result);
    }

    @Test
    public void testUpdate() {
        OrgUpdateByIdApi api = new OrgUpdateByIdApi();
        OrgUpdateDto dto = new OrgUpdateDto();
        dto.setId(43373586232915972L);
        dto.setName("13888888888" + new Random().nextInt(5000));
        dto.setOrgType("20");
        api.setBizModel(dto);
        Result<OrgResp> result = client.execute(api);
        logResult(result);
    }

    @Test
    public void testPage() {
        OrgPageApi api = new OrgPageApi();
        PageParams<OrgQuery> dto = new PageParams<>(1, 5);
        OrgQuery query = new OrgQuery();
        query.setName("13888888888");
        dto.setModel(query);
        api.setBizModel(dto);

        Result<Page<OrgResp>> result = client.execute(api);
        logResult(result);
    }
}
