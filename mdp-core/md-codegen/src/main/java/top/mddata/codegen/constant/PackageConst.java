/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package top.mddata.codegen.constant;

/** 自动导包路径 */
public interface PackageConst {
    String VALIDATED = "org.springframework.validation.annotation.Validated";
    String R = "top.mddata.base.base.R";
    String BASE_ENTITY = "top.mddata.base.base.entity.BaseEntity";
    String PAGE_PARAMS = "top.mddata.base.mvcflex.request.PageParams";
    String CONTROLLER_UTIL = "top.mddata.base.mvcflex.utils.WrapperUtil";

    String QUERY_WRAPPER = "com.mybatisflex.core.query.QueryWrapper";
    String PAGE = "com.mybatisflex.core.paginate.Page";

    String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
    String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    String POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping";
    String PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping";
    String GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping";
    String DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping";
    String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    String CONTROLLER = "org.springframework.stereotype.Controller";
    String AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired";


    String API = "io.swagger.annotations.Api";
    String API_OPERATION = "io.swagger.annotations.ApiOperation";
    String API_PARAM = "io.swagger.annotations.ApiParam";
    String TAG = "io.swagger.v3.oas.annotations.tags.Tag";
    String OPERATION = "io.swagger.v3.oas.annotations.Operation";
    String PARAMETER = "io.swagger.v3.oas.annotations.Parameter";
}
