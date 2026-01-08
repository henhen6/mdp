package top.mddata.sdk.test.user;

import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.request.PageParams;
import top.mddata.sdk.core.response.Page;
import top.mddata.sdk.simple.api.user.UserBatchSaveApi;
import top.mddata.sdk.simple.api.user.UserGetByIdApi;
import top.mddata.sdk.simple.api.user.UserPageApi;
import top.mddata.sdk.simple.api.user.UserUpdateByIdApi;
import top.mddata.sdk.simple.request.user.UserBatchSaveDto;
import top.mddata.sdk.simple.request.user.UserGetByIdDto;
import top.mddata.sdk.simple.request.user.UserQuery;
import top.mddata.sdk.simple.request.user.UserSaveDto;
import top.mddata.sdk.simple.request.user.UserUpdateDto;
import top.mddata.sdk.simple.response.user.UserBatchSaveResp;
import top.mddata.sdk.simple.response.user.UserResp;
import top.mddata.sdk.test.BaseTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author henhen
 * @since 2026/1/8 23:45
 */

public class UserTest extends BaseTest {


    public void testGetById() {
        UserGetByIdApi api = new UserGetByIdApi();
        api.setBizModel(new UserGetByIdDto().setId(680083598598475778L));
        Result<UserResp> result = client.execute(api);
        logResult(result);
    }

    public void testSave() {
        List<UserSaveDto> list = new ArrayList<>();
        UserSaveDto user1 = new UserSaveDto();
        user1.setUsername("test" + new Random().nextInt(5000));
        user1.setName("test1");
        list.add(user1);
        user1 = new UserSaveDto();
        user1.setUsername("test" + new Random().nextInt(5000));
        user1.setName("test2");
        list.add(user1);

        UserBatchSaveDto dto = new UserBatchSaveDto();
        dto.setList(list);
        // 创建请求对象
        UserBatchSaveApi param = new UserBatchSaveApi();
        param.setBizModel(dto);
        // 这里提供了 mdp-openapi-server 的一个接口地址作为测试
        param.setNotifyUrl("http://localhost:18766/notify/callback22333");

        // 发送请求
        Result<UserBatchSaveResp> result = client.execute(param);

        logResult(result);
    }


    public void testUpdate() {
        UserUpdateByIdApi api = new UserUpdateByIdApi();
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(42814334246315008L);
        dto.setPhone("13888888888" + new Random().nextInt(5000));
        api.setBizModel(dto);
        Result<UserResp> result = client.execute(api);
        logResult(result);
    }

    public void testPage() {
        UserPageApi api = new UserPageApi();
        PageParams<UserQuery> dto = new PageParams<>(1, 5);
        UserQuery query = new UserQuery();
        query.setPhone("13888888888");
        dto.setModel(query);
        api.setBizModel(dto);

        Result<Page<UserResp>> result = client.execute(api);
        logResult(result);
    }
}
