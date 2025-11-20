package top.mddata.base.utils;

import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.TreeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author henhen6
 * @since 2025/8/4 16:15
 */
public class MyTreeUtilTest {

    public static void main(String[] args) {
        testBuildTree1();
    }

    private static void testBuildTree1() {
        List<Ml> list = new ArrayList<>();

        Ml ml1 = new Ml().setName("n-1").setAge("11");
        ml1.setParentId(null).setWeight(3).setId(1L);
        list.add(ml1);

        ml1 = new Ml().setName("n-2").setAge("12");
        ml1.setParentId(null).setWeight(2).setId(2L);
        list.add(ml1);

        ml1 = new Ml().setName("n-3").setAge("13");
        ml1.setParentId(null).setWeight(1).setId(3L);
        list.add(ml1);


        ml1 = new Ml().setName("n-2-1").setAge("12-1");
        ml1.setParentId(2L).setWeight(5).setId(21L);
        list.add(ml1);

        ml1 = new Ml().setName("n-2-2").setAge("12-2");
        ml1.setParentId(2L).setWeight(1).setId(22L);
        list.add(ml1);

        ml1 = new Ml().setName("n-2-2-1").setAge("12-2-1");
        ml1.setParentId(22L).setWeight(5).setId(221L);
        list.add(ml1);

        List<Ml> mls = MyTreeUtil.buildTreeEntity(list, Ml::new);
        System.out.println(mls);
    }


    @Data
    @Accessors(chain = true)
    static class Ml extends TreeEntity<Long, Ml> {
        private String name;
        private String age;

    }
}
