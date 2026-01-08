package top.mddata.sdk.core.response;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页对象封装。
 *
 * @param <T> 数据类型
 */
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int INIT_VALUE = -1;

    /**
     * 当前页数据。
     */
    private List<T> records = Collections.emptyList();

    /**
     * 当前页码。
     * -- GETTER --
     * 获取当前页码。
     *
     * @return 页码
     */
    private long pageNumber = 1;

    /**
     * 每页数据数量。
     * -- GETTER --
     * 获取当前每页数据数量。
     *
     * @return 每页数据数量
     */
    private long pageSize;

    /**
     * 总页数。
     * -- GETTER --
     * 获取数据总数。
     *
     * @return 数据总数
     */
    private long totalPage = INIT_VALUE;

    /**
     * 总数据数量。
     * -- GETTER --
     * 获取数据总数。
     *
     * @return 数据总数
     */
    private long totalRow = INIT_VALUE;

    /**
     * 是否优化分页查询 COUNT 语句。
     * -- SETTER --
     * 设置是否自动优化 COUNT 查询语句。
     *
     * @param optimizeCountQuery 是否优化
     */
    private boolean optimizeCountQuery = true;

    /**
     * 创建分页对象。
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页数据数量
     * @param <T>        数据类型
     * @return 分页对象
     */
    public static <T> Page<T> of(Number pageNumber, Number pageSize) {
        return new Page<>(pageNumber, pageSize);
    }

    /**
     * 创建分页对象。
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页数据数量
     * @param totalRow   总数据数量
     * @param <T>        数据类型
     * @return 分页对象
     */
    public static <T> Page<T> of(Number pageNumber, Number pageSize, Number totalRow) {
        return new Page<>(pageNumber, pageSize, totalRow);
    }

    /**
     * 创建分页对象。
     */
    public Page() {
    }

    /**
     * 创建分页对象。
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页数据数量
     */
    public Page(Number pageNumber, Number pageSize) {
        this.setPageNumber(pageNumber.longValue());
        this.setPageSize(pageSize.longValue());
    }

    /**
     * 创建分页对象。
     *
     * @param pageNumber 当前页码
     * @param pageSize   每页数据数量
     * @param totalRow   总数居数量
     */
    public Page(Number pageNumber, Number pageSize, Number totalRow) {
        this.setPageNumber(pageNumber.longValue());
        this.setPageSize(pageSize.longValue());
        this.setTotalRow(totalRow.longValue());
    }

    /**
     * 创建分页对象。
     *
     * @param records    当前页数据
     * @param pageNumber 当前页码
     * @param pageSize   每页数据数量
     * @param totalRow   总数居数量
     */
    public Page(List<T> records, Number pageNumber, Number pageSize, Number totalRow) {
        this.setRecords(records);
        this.setPageNumber(pageNumber.longValue());
        this.setPageSize(pageSize.longValue());
        this.setTotalRow(totalRow.longValue());
    }


    /**
     * 设置当前页的数据。
     *
     * @param records 当前页的数据
     */
    public void setRecords(List<T> records) {
        if (records == null) {
            records = Collections.emptyList();
        }
        this.records = records;
    }

    /**
     * 设置当前页码。
     *
     * @param pageNumber 页码
     */
    public void setPageNumber(long pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must greater than or equal 1，current value is: " + pageNumber);
        }
        this.pageNumber = pageNumber;
    }

    /**
     * 设置当前每页数据数量。
     *
     * @param pageSize 每页数据数量
     */
    public void setPageSize(long pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must greater than 0，current value is: " + pageSize);
        }
        this.pageSize = pageSize;
        this.calcTotalPage();
    }

    /**
     * 设置数据总数。
     *
     * @param totalRow 数据总数
     */
    public void setTotalRow(long totalRow) {
        this.totalRow = totalRow;
        this.calcTotalPage();
    }

    /**
     * 计算总页码。
     */
    private void calcTotalPage() {
        if (pageSize < 0 || totalRow < 0) {
            totalPage = INIT_VALUE;
        } else {
            totalPage = totalRow % pageSize == 0 ? (totalRow / pageSize) : (totalRow / pageSize + 1);
        }
    }

    /**
     * 当前页是否有记录（有内容）。
     *
     * @return {@code true} 有内容，{@code false} 没有内容
     */
    public boolean hasRecords() {
        return getTotalRow() > 0 && getPageNumber() <= getTotalPage();
    }

    /**
     * 是否存在下一页。
     *
     * @return {@code true} 存在下一页，{@code false} 不存在下一页
     */
    public boolean hasNext() {
        return getTotalPage() != 0 && getPageNumber() < getTotalPage();
    }

    /**
     * 是否存在上一页。
     *
     * @return {@code true} 存在上一页，{@code false} 不存在上一页
     */
    public boolean hasPrevious() {
        return getPageNumber() > 1;
    }

    /**
     * 获取当前分页偏移量。
     *
     * @return 偏移量
     */
    public long offset() {
        return getPageSize() * (getPageNumber() - 1);
    }

    /**
     * 是否自动优化 COUNT 查询语句（默认优化）。
     *
     * @return {@code true} 优化，{@code false} 不优化
     */
    public boolean needOptimizeCountQuery() {
        return optimizeCountQuery;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getPageNumber() {
        return pageNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public Page<T> setTotalPage(long totalPage) {
        this.totalPage = totalPage;
        return this;
    }

    public long getTotalRow() {
        return totalRow;
    }

    public boolean isOptimizeCountQuery() {
        return optimizeCountQuery;
    }

    public Page<T> setOptimizeCountQuery(boolean optimizeCountQuery) {
        this.optimizeCountQuery = optimizeCountQuery;
        return this;
    }

    @Override
    public String toString() {
        return "Page{" +
               "pageNumber=" + pageNumber +
               ", pageSize=" + pageSize +
               ", totalPage=" + totalPage +
               ", totalRow=" + totalRow +
               ", records=" + records +
               '}';
    }
}
