package top.mddata.console.service.message.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import top.mddata.common.constant.FileObjectType;
import top.mddata.console.dto.message.MsgTaskDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.entity.message.MsgTask;
import top.mddata.console.service.message.MsgTaskRecipientService;
import top.mddata.console.service.message.MsgTemplateService;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MsgTaskServiceImplTest {

    private FileService fileService;
    private MsgTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        fileService = Mockito.mock(FileService.class);
        service = new MsgTaskServiceImpl(
                Mockito.mock(MsgTaskRecipientService.class),
                Mockito.mock(MsgTemplateService.class),
                Mockito.mock(ConfigService.class),
                fileService);
    }

    private MsgTask entityOf(Long id) {
        MsgTask entity = new MsgTask();
        entity.setId(id);
        return entity;
    }

    @Test
    void saveAfter时应按内容文件id关联附件() {
        MsgTaskDto dto = new MsgTaskDto().setContentFileIds(List.of(1L, 3L, 4L));

        service.saveAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileService).relateFilesToBiz(captor.capture());
        RelateFilesToBizDto actual = captor.getValue();
        assertEquals(FileObjectType.Console.MSG_TASK_CONTENT, actual.getObjectType());
        assertEquals(99L, actual.getObjectId());
        assertEquals(List.of(1L, 3L, 4L), actual.getKeepFileIds());
    }

    @Test
    void updateAfter时应按内容文件id关联附件() {
        MsgTaskDto dto = new MsgTaskDto().setContentFileIds(List.of(1L, 3L, 5L));

        service.updateAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileService).relateFilesToBiz(captor.capture());
        assertEquals(List.of(1L, 3L, 5L), captor.getValue().getKeepFileIds());
    }

    @Test
    void contentFileIds为空列表时应清理全部关联() {
        // 用户把内容里的图片删光：空列表是有效指令，不是"未携带"
        MsgTaskDto dto = new MsgTaskDto().setContentFileIds(List.of());

        service.updateAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileService).relateFilesToBiz(captor.capture());
        assertTrue(captor.getValue().getKeepFileIds().isEmpty());
    }

    @Test
    void contentFileIds为null时跳过关联() {
        // 老版本客户端未携带该字段，不能误删已关联的文件
        service.saveAfter(new MsgTaskDto(), entityOf(99L));
        service.updateAfter(new MsgTaskDto(), entityOf(99L));

        Mockito.verifyNoInteractions(fileService);
    }

    @Test
    void saveAfter传入实体而非DTO时跳过关联() {
        // publish 流程以实体调 save()，钩子收到的不是 DTO，不能强转报错
        service.saveAfter(entityOf(99L), entityOf(99L));

        Mockito.verifyNoInteractions(fileService);
    }
}
