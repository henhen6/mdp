package top.mddata.base.mvcflex.controller;

import cn.hutool.core.util.URLUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.base.utils.ContextUtil;

import java.io.IOException;

/**
 * 基础接口
 *
 * @param <Entity> 实体
 * @author henhen6
 * @since 2020年03月07日21:56:32
 */
public interface BaseController<Entity extends BaseEntity<?>> {


    /**
     * 获取Service
     *
     * @return Service
     */
    SuperService<Entity> getSuperService();

    /**
     * 获取实体的类型
     *
     * @return 实体的类型
     */
    Class<Entity> getEntityClass();

    /**
     * 获取当前id
     *
     * @return userId
     */
    default Long getUserId() {
        return ContextUtil.getUserId();
    }


    /**
     * 生成zip文件
     *
     * @param data     数据
     * @param fileName 文件名
     * @param response 响应
     * @throws IOException
     */
    @SneakyThrows
    default void write(byte[] data, String fileName, HttpServletResponse response) {
        response.reset();
        response.setHeader("Content-Disposition", "attachment;filename=" + URLUtil.encode(fileName));
        response.addHeader("Content-Length", String.valueOf(data.length));
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.getOutputStream().write(data);
    }

}
