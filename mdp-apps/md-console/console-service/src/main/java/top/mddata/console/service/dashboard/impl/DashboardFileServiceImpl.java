package top.mddata.console.service.dashboard.impl;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.utils.DefValueHelper;
import top.mddata.common.constant.FileObjectType;
import top.mddata.console.enumeration.system.FileTypeEnum;
import top.mddata.console.mapper.system.FileMapper;
import top.mddata.console.service.dashboard.DashboardFileService;
import top.mddata.console.vo.dashboard.DistributionVo;
import top.mddata.console.vo.dashboard.FileTrendVo;
import top.mddata.console.vo.dashboard.OverviewFileVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /** 默认日期范围 */
    private static final int DEFAULT_DAYS = 6;

    private final FileMapper fileMapper;

    @Override
    public OverviewFileVo getOverviewFile() {
        OverviewFileVo vo = new OverviewFileVo();

        vo.setFileCount(fileMapper.selectCountByQuery(com.mybatisflex.core.query.QueryWrapper.create()));

        Long totalSize = fileMapper.sumFileSize();
        vo.setFileTotalSize(DefValueHelper.nvl(totalSize, 0L));

        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDateTime monthStart = LocalDateTime.of(firstDay, LocalTime.MIN);

        Map<String, Object> monthStat = fileMapper.statAfter(monthStart);
        vo.setMonthFileCount(Convert.toLong(monthStat != null ? monthStat.get("fileCount") : null));
        vo.setMonthTotalSize(Convert.toLong(monthStat != null ? monthStat.get("totalSize") : null));

        Map<String, Object> tempStat = fileMapper.statByObjectType(FileObjectType.TEMP_OBJECT_TYPE);
        vo.setTempFileCount(Convert.toLong(tempStat != null ? tempStat.get("fileCount") : null));
        vo.setTempFileSize(Convert.toLong(tempStat != null ? tempStat.get("totalSize") : null));

        return vo;
    }

    @Override
    public List<DistributionVo> getFileTypeDistribution() {
        List<Map<String, Object>> rawList = fileMapper.countByFileType();
        return convertDistributionList(rawList, code -> convertFileType(Convert.toLong(code)));
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
    public List<FileTrendVo> getTrend(LocalDate startDate, LocalDate endDate) {
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(DEFAULT_DAYS);

        LocalDateTime startTime = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> rawList = fileMapper.countByDayRange(startTime, endTime);
        Map<String, long[]> dateMap = new HashMap<>();
        for (Map<String, Object> raw : rawList) {
            String date = Convert.toStr(raw.get("date"));
            long count = Convert.toLong(raw.get("fileCount"));
            long size = Convert.toLong(raw.get("totalSize"));
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

    /** 转换分发列表（带名称转换器） */
    private List<DistributionVo> convertDistributionList(List<Map<String, Object>> rawList,
                                                         java.util.function.Function<Object, String> nameConverter) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(nameConverter.apply(raw.get("code")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
    }

    /** 转换为分发列表（无转换器，直接用name字段） */
    private List<DistributionVo> toDistributionList(List<Map<String, Object>> rawList) {
        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }
        long total = rawList.stream().mapToLong(raw -> Convert.toLong(raw.get("count"))).sum();
        return rawList.stream().map(raw -> {
            DistributionVo vo = new DistributionVo();
            vo.setName(Convert.toStr(raw.get("name")));
            long count = Convert.toLong(raw.get("count"));
            vo.setCount(count);
            vo.setPercent(DefValueHelper.calcPercent(count, total));
            return vo;
        }).collect(Collectors.toList());
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
}
