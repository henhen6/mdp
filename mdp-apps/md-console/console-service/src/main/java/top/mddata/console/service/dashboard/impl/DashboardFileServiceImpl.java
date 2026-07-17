package top.mddata.console.service.dashboard.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.common.constant.FileObjectType;
import top.mddata.console.enumeration.system.FileTypeEnum;
import top.mddata.console.mapper.system.FileMapper;
import top.mddata.console.service.dashboard.DashboardFileService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.FileTrendVo;
import top.mddata.console.vo.dashboard.OverviewFileVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件存储统计 服务层实现
 *
 * <p>说明：mdc_file 表没有 deleted_at 字段，
 * MyBatis-Flex 不会自动追加删除过滤条件。</p>
 *
 * @author henhen6
 * @since 2026-07-10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardFileServiceImpl implements DashboardFileService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FileMapper fileMapper;

    @Override
    public OverviewFileVo getOverviewFile() {
        OverviewFileVo vo = new OverviewFileVo();

        // 文件总数（mdc_file 没有 deleted_at 字段）
        vo.setFileCount(fileMapper.selectCountByQuery(com.mybatisflex.core.query.QueryWrapper.create()));

        // 文件总容量
        Long totalSize = fileMapper.sumFileSize();
        vo.setFileTotalSize(totalSize != null ? totalSize : 0L);

        // 本月起止时间
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthStart = LocalDateTime.of(firstDay, LocalTime.MIN);

        Map<String, Object> monthStat = fileMapper.statAfter(monthStart);
        if (monthStat != null) {
            vo.setMonthFileCount(toLong(monthStat.get("fileCount")));
            vo.setMonthTotalSize(toLong(monthStat.get("totalSize")));
        } else {
            vo.setMonthFileCount(0L);
            vo.setMonthTotalSize(0L);
        }

        // 临时文件数量和容量
        Map<String, Object> tempStat = fileMapper.statByObjectType(FileObjectType.TEMP_OBJECT_TYPE);
        if (tempStat != null) {
            vo.setTempFileCount(toLong(tempStat.get("fileCount")));
            vo.setTempFileSize(toLong(tempStat.get("totalSize")));
        } else {
            vo.setTempFileCount(0L);
            vo.setTempFileSize(0L);
        }

        return vo;
    }

    @Override
    public List<DistributionVo> getFileTypeDistribution() {
        return toFileTypeDistributionList(fileMapper.countByFileType());
    }

    private List<DistributionVo> toFileTypeDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DistributionVo vo = new DistributionVo();
            vo.setName(convertFileType(toLong(raw.get("code"))));
            long count = toLong(raw.get("count"));
            vo.setCount(count);
            if (total > 0) {
                double percent = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                vo.setPercent(percent);
            } else {
                vo.setPercent(0d);
            }
            result.add(vo);
        }
        return result;
    }

    private String convertFileType(Long code) {
        if (code == null) {
            return null;
        }
        for (FileTypeEnum enumVal : FileTypeEnum.values()) {
            if (enumVal.getCode().equals(code.intValue())) {
                return enumVal.getDesc();
            }
        }
        return "其他-" + code;
    }

    @Override
    public List<DistributionVo> getObjectTypeDistribution() {
        return toDistributionList(fileMapper.countByObjectType());
    }

    @Override
    public List<DistributionVo> getPlatformDistribution() {
        return toDistributionList(fileMapper.countByPlatform());
    }

    @Override
    public List<DistributionVo> getSizeDistribution() {
        return toDistributionList(fileMapper.countBySizeRange());
    }

    @Override
    public List<FileTrendVo> getTrend(String startDate, String endDate) {
        // 默认近7天
        LocalDate end = (endDate == null || endDate.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(endDate, DATE_FORMATTER);
        LocalDate start = (startDate == null || startDate.isBlank())
                ? end.minusDays(6)
                : LocalDate.parse(startDate, DATE_FORMATTER);

        LocalDateTime startTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> rawList = fileMapper.countByDayRange(startTime, endTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = toStr(raw.get("date"));
            long count = toLong(raw.get("fileCount"));
            long size = toLong(raw.get("totalSize"));
            dateMap.put(date, new long[]{count, size});
        }

        List<FileTrendVo> result = new ArrayList<>();
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        for (int i = 0; i < daysBetween; i++) {
            LocalDate d = start.plusDays(i);
            String key = d.toString();
            long[] arr = dateMap.getOrDefault(key, new long[]{0L, 0L});
            FileTrendVo vo = new FileTrendVo();
            vo.setDate(key);
            vo.setFileCount(arr[0]);
            vo.setTotalSize(arr[1]);
            result.add(vo);
        }
        return result;
    }

    private List<DistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = 0L;
        for (Map<String, Object> raw : rawList) {
            total += toLong(raw.get("count"));
        }
        List<DistributionVo> result = new ArrayList<>(rawList.size());
        for (Map<String, Object> raw : rawList) {
            DistributionVo vo = new DistributionVo();
            vo.setName(toStr(raw.get("name")));
            long count = toLong(raw.get("count"));
            vo.setCount(count);
            if (total > 0) {
                double percent = BigDecimal.valueOf(count)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                        .doubleValue();
                vo.setPercent(percent);
            } else {
                vo.setPercent(0d);
            }
            result.add(vo);
        }
        return result;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String toStr(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
