package top.mddata.base.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbRuntimeException;
import cn.hutool.db.DbUtil;
import cn.hutool.db.meta.MetaUtil;
import cn.hutool.db.meta.Table;
import cn.hutool.db.meta.TableType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库元数据增强工具类
 *
 * @author henhen6
 * @since 2024/6/30 16:42
 */
public class MetaPlusUtil {
    /**
     * 获得所有表名
     *
     * @param ds 数据源
     * @return 表名列表
     */
    public static List<Table> getTables(DataSource ds) {
        final List<Table> tables = new ArrayList<>();
        Connection conn = null;
        try {
            conn = ds.getConnection();
            // catalog和schema获取失败默认使用null代替
            String catalog = MetaUtil.getCatalog(conn);
            String schema = MetaUtil.getSchema(conn);

            final DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(catalog, schema, null, Convert.toStrArray(TableType.TABLE))) {
                if (null != rs) {
                    String tableName;
                    while (rs.next()) {
                        tableName = rs.getString("TABLE_NAME");
                        if (StrUtil.isNotBlank(tableName)) {
                            final Table table = Table.create(tableName);
                            table.setCatalog(catalog);
                            table.setSchema(schema);
                            table.setComment(rs.getString("REMARKS"));
                            // 获得主键
                            try (ResultSet rsPk = metaData.getPrimaryKeys(catalog, schema, tableName)) {
                                if (null != rsPk) {
                                    while (rsPk.next()) {
                                        table.addPk(rsPk.getString("COLUMN_NAME"));
                                    }
                                }
                            }
                            tables.add(table);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DbRuntimeException("Get tables error!", e);
        } finally {
            DbUtil.close(conn);
        }
        return tables;
    }

}
