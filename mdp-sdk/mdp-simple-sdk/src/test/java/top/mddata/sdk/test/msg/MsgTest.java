package top.mddata.sdk.test.msg;

import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.request.Kv;
import top.mddata.sdk.simple.api.msg.SendMailApi;
import top.mddata.sdk.simple.api.msg.SendNoticeApi;
import top.mddata.sdk.simple.api.msg.SendSmsApi;
import top.mddata.sdk.simple.request.msg.SendMailDto;
import top.mddata.sdk.simple.request.msg.SendNoticeDto;
import top.mddata.sdk.simple.request.msg.SendSmsDto;
import top.mddata.sdk.test.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author henhen
 * @since 2026/1/8 23:45
 */
public class MsgTest extends BaseTest {

    public void testSendSms() {
        List<Kv> paramList = new ArrayList<>();
        paramList.add(new Kv("code", "1113"));

        SendSmsDto dto = new SendSmsDto();
        dto.setRecipientList(Arrays.asList("13800000001", "13800000002"))
                .setTiming(false)
                .setTemplateKey("ttt")
                .setParamList(paramList);

        SendSmsApi api = new SendSmsApi();
        api.setBizModel(dto);
        Result<Void> result = client.execute(api);
        logResult(result);
    }


    public void testSendEmail() {
        List<Kv> paramList = new ArrayList<>();
        paramList.add(new Kv("code", "1113"));

        SendMailDto dto = new SendMailDto();
        dto.setRecipientList(Arrays.asList("244387066@qq.com"))
                .setTiming(false)
                .setTemplateKey("ttt")
                .setParamList(paramList);

        SendMailApi api = new SendMailApi();
        api.setBizModel(dto);
        Result<Void> result = client.execute(api);
        logResult(result);
    }


    public void testSendNotice() {
        List<Kv> paramList = new ArrayList<>();
        paramList.add(new Kv("code", "1113"));

        SendNoticeDto dto = new SendNoticeDto();
        dto.setMsgCategory(1)
                .setRecipientIdList(Arrays.asList(680083598598475778L, 680083598598475779L, 680083598598475780L))
                .setRecipientScope(1)
                .setTiming(false)
                .setTemplateKey("ttt")
                .setParamList(paramList);

        SendNoticeApi api = new SendNoticeApi();
        api.setBizModel(dto);
        Result<Void> result = client.execute(api);
        logResult(result);
    }

}
