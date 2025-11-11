package top.mddata.base.db.typehandler;


import org.apache.ibatis.type.Alias;

/**
 * 仅仅用于like查询
 *
 * @author henhen6
 */
@Alias("rightLike")
public class RightLikeTypeHandler extends BaseLikeTypeHandler {
    public RightLikeTypeHandler() {
        super(SqlLike.RIGHT);
    }
}

