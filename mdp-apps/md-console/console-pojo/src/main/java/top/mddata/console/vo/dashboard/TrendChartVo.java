package top.mddata.console.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户增长趋势（多曲线）VO
 *
 * @author henhen6
 * @since 2026-09-30
 */
@Data
@Schema(description = "用户增长趋势（多曲线）")
public class TrendChartVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "日期列表，格式 yyyy-MM-dd，升序")
    private List<String> dates;

    @Schema(description = "曲线列表；运营=4条（总用户/总公司/开发者/运营），其他性质=1条（新增用户）")
    private List<Series> series;

    /**
     * 趋势曲线
     */
    @Data
    @Schema(description = "趋势曲线")
    public static class Series implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "曲线名称")
        private String name;

        @Schema(description = "各日期对应数值，与 dates 等长，缺日期补 0")
        private List<Long> data;
    }
}
