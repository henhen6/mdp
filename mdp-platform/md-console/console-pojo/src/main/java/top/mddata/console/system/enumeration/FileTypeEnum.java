
package top.mddata.console.system.enumeration;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import top.mddata.base.interfaces.BaseEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文件类型枚举
 *
 * @author henhen6
 * @date 2025年10月22日22:47:55
 */
@Getter
@RequiredArgsConstructor
public enum FileTypeEnum implements BaseEnum<Integer> {

    /**
     * 目录
     */
    DIR(0, "目录", Collections.emptyList()),

    /**
     * 其他
     */
    UNKNOWN(1, "其他", Collections.emptyList()),

    /**
     * 图片
     */
    IMAGE(2, "图片", List
            .of("jpg", "jpeg", "png", "gif", "bmp", "webp", "ico", "psd", "tiff", "dwg", "jxr", "apng", "xcf")),

    /**
     * 文档
     */
    DOC(3, "文档", List.of("txt", "pdf", "doc", "xls", "ppt", "docx", "xlsx", "pptx")),

    /**
     * 视频
     */
    VIDEO(4, "视频", List.of("mp4", "avi", "mkv", "flv", "webm", "wmv", "m4v", "mov", "mpg", "rmvb", "3gp")),

    /**
     * 音频
     */
    AUDIO(5, "音频", List.of("mp3", "flac", "wav", "ogg", "midi", "m4a", "aac", "amr", "ac3", "aiff"));

    private final Integer code;
    private final String desc;
    private final List<String> extensions;

    /**
     * 根据扩展名查询
     *
     * @param extension 扩展名
     * @return 文件类型
     */
    public static FileTypeEnum getByExtension(String extension) {
        return Arrays.stream(FileTypeEnum.values())
                .filter(t -> t.getExtensions().contains(StrUtil.emptyIfNull(extension).toLowerCase()))
                .findFirst()
                .orElse(FileTypeEnum.UNKNOWN);
    }

    /**
     * 获取所有扩展名
     *
     * @return 所有扩展名
     */
    public static List<String> getAllExtensions() {
        return Arrays.stream(FileTypeEnum.values()).flatMap(t -> t.getExtensions().stream()).toList();
    }
}
