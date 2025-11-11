package top.mddata.base.db.typehandler;

import org.apache.ibatis.type.Alias;

/**
 * 仅仅用于like查询
 *
 * @author henhen6
 */
@Alias("fullLike")
public class FullLikeTypeHandler extends BaseLikeTypeHandler {
    public FullLikeTypeHandler() {
        super(SqlLike.DEFAULT);
    }
}
