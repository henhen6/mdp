package top.mddata;

import cn.hutool.core.lang.Dict;
import org.dromara.x.file.storage.core.FileInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.utils.FileConvert;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author henhen6
 * @since 2025/11/27 21:19
 */

@SpringBootTest(classes = TestBootServerApplication.class)
// 2. 激活 test 环境，读取 application-test.yml
//@ActiveProfiles("dev")
public class MapStructSpringTest {
    @Autowired
    private FileConvert fileConvert;

    @Test
    public void testToTarget() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setBasePath("aaa");
        Map<String, String> objectObjectMap = new HashMap<>();
        objectObjectMap.put("aa", "111");
        fileInfo.setMetadata(objectObjectMap);

        File file = new File();
        file.setId(111L);
        file.setAttr("{}");
        file.setHashInfo("{\"sha-256\":\"58816f76bc01c80a42cbb339a62ad19282d1117eb91ed9cf159e1fd1f4969342\"}");

        FileInfo target = fileConvert.toTarget(file);
        System.out.println(target);
    }

    @Test
    public void testToSource() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setBasePath("aaa");
        Map<String, String> objectObjectMap = new HashMap<>();
        objectObjectMap.put("aa", "111");
        fileInfo.setMetadata(objectObjectMap);
        Dict dict = Dict.of("aaaa", "222");
        fileInfo.setAttr(dict);
        fileInfo.setSize(123L);
        fileInfo.setFileAcl(new String("123123"));

        File file = fileConvert.toSource(fileInfo);
        System.out.println(file);
    }
}
