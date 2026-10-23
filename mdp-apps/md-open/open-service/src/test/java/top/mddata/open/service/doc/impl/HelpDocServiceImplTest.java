package top.mddata.open.service.doc.impl;

import com.baidu.fsg.uid.UidGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import top.mddata.common.constant.FileObjectType;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.facade.system.FileFacade;
import top.mddata.open.dto.admin.HelpDocDto;
import top.mddata.open.entity.admin.HelpDoc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HelpDocServiceImplTest {

    private FileFacade fileFacade;
    private HelpDocServiceImpl service;

    @BeforeEach
    void setUp() {
        fileFacade = Mockito.mock(FileFacade.class);
        service = new HelpDocServiceImpl(Mockito.mock(UidGenerator.class), fileFacade);
    }

    private HelpDoc entityOf(Long id) {
        HelpDoc entity = new HelpDoc();
        entity.setId(id);
        return entity;
    }

    @Test
    void saveAfter时应按内容文件id关联附件() {
        HelpDocDto dto = new HelpDocDto().setContentFileIds(List.of(1L, 3L, 4L));

        service.saveAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileFacade).relateFilesToBiz(captor.capture());
        RelateFilesToBizDto actual = captor.getValue();
        assertEquals(FileObjectType.Open.HELP_DOC_CONTENT, actual.getObjectType());
        assertEquals(99L, actual.getObjectId());
        assertEquals(List.of(1L, 3L, 4L), actual.getKeepFileIds());
    }

    @Test
    void updateAfter时应按内容文件id关联附件() {
        HelpDocDto dto = new HelpDocDto().setContentFileIds(List.of(1L, 3L, 5L));

        service.updateAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileFacade).relateFilesToBiz(captor.capture());
        assertEquals(List.of(1L, 3L, 5L), captor.getValue().getKeepFileIds());
    }

    @Test
    void contentFileIds为空列表时应清理全部关联() {
        // 用户把内容里的图片删光：空列表是有效指令，不是"未携带"
        HelpDocDto dto = new HelpDocDto().setContentFileIds(List.of());

        service.updateAfter(dto, entityOf(99L));

        ArgumentCaptor<RelateFilesToBizDto> captor = ArgumentCaptor.forClass(RelateFilesToBizDto.class);
        Mockito.verify(fileFacade).relateFilesToBiz(captor.capture());
        assertTrue(captor.getValue().getKeepFileIds().isEmpty());
    }

    @Test
    void contentFileIds为null时跳过关联() {
        // 老版本客户端未携带该字段，不能误删已关联的文件
        service.saveAfter(new HelpDocDto(), entityOf(99L));
        service.updateAfter(new HelpDocDto(), entityOf(99L));

        Mockito.verifyNoInteractions(fileFacade);
    }
}
