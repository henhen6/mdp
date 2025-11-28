package top.mddata;

import org.dromara.x.file.storage.core.FileInfo;
import org.junit.jupiter.api.Test;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.utils.FileConvert;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tangyh
 * @since 2025/11/27 21:19
 */
public class MapStructTest {
    @Test
    public void test1() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setBasePath("aaa");
        Map<String, String> objectObjectMap = new HashMap<>();
        objectObjectMap.put("aa", "111");
        fileInfo.setMetadata(objectObjectMap);

//        File source = FileConvert.INSTANCE.toSource(fileInfo);
//        System.out.println(source);

        File file = new File();
        file.setId(111L);
        file.setAttr("{}");
        file.setHashInfo("{\"sha-256\":\"58816f76bc01c80a42cbb339a62ad19282d1117eb91ed9cf159e1fd1f4969342\"}");

        FileInfo target = FileConvert.INSTANCE.toTarget(file);
        System.out.println(target);
    }
}
