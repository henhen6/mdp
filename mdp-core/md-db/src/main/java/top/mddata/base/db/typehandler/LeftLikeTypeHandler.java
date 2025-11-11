package top.mddata.base.db.typehandler;


import org.apache.ibatis.type.Alias;

/**
 * 仅仅用于like查询
 *
 * @author henhen6
 */
@Alias("leftLike")
public class LeftLikeTypeHandler extends BaseLikeTypeHandler {
    public LeftLikeTypeHandler() {
        super(SqlLike.LEFT);
    }
}
