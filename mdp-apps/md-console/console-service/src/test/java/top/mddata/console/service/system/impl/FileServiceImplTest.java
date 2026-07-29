package top.mddata.console.service.system.impl;

import com.mybatisflex.core.query.QueryWrapper;
import org.dromara.x.file.storage.core.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import top.mddata.base.exception.BizException;
import top.mddata.console.dto.system.FilePartDto;
import top.mddata.console.entity.system.FilePart;
import top.mddata.console.service.system.FilePartService;
import top.mddata.console.service.system.convert.FileConvert;
import top.mddata.console.service.system.properties.FileProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FileServiceImpl 分片上传功能单元测试
 *
 * @author henhen6
 * @since 2026-07-28
 */
class FileServiceImplTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileProperties fileProperties;

    @Mock
    private FileConvert fileConvert;

    @Mock
    private FilePartService filePartService;

    private FileServiceImpl fileService;
    private AutoCloseable mocks;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        fileService = new FileServiceImpl(fileStorageService, fileProperties, fileConvert, filePartService);

        // 模拟 FileProperties 的 validSuffix 方法返回 true
        when(fileProperties.validSuffix(anyString())).thenReturn(true);
        when(fileProperties.getChunkSize()).thenReturn(5); // 5MB
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // ==================== initPartUpload 测试 ====================

    @Nested
    @DisplayName("initPartUpload - 初始化分片上传")
    class InitPartUploadTests {

        @Test
        @DisplayName("正常初始化分片上传")
        void initPartUpload_Success() {
            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("test.pdf");
            dto.setFileSize(10 * 1024 * 1024L); // 10MB
            dto.setObjectType("contract");
            dto.setObjectId(123L);
            dto.setPlatform("local");

            FilePartDto.InitPartUploadResp resp = fileService.initPartUpload(dto);

            assertNotNull(resp);
            assertNotNull(resp.getUploadId());
            assertEquals(32, resp.getUploadId().length()); // UUID 无横杠长度
            assertEquals(5 * 1024 * 1024L, resp.getChunkSize()); // 5MB
            assertEquals(2, resp.getTotalChunks()); // 10MB / 5MB = 2 chunks
        }

        @Test
        @DisplayName("文件大小为0时总分片数为0")
        void initPartUpload_ZeroFileSize() {
            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("empty.txt");
            dto.setFileSize(0L);

            FilePartDto.InitPartUploadResp resp = fileService.initPartUpload(dto);

            assertNotNull(resp);
            assertEquals(0, resp.getTotalChunks());
        }

        @Test
        @DisplayName("小于分片大小的文件只有1个分片")
        void initPartUpload_SingleChunk() {
            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("small.pdf");
            dto.setFileSize(1024 * 1024L); // 1MB < 5MB

            FilePartDto.InitPartUploadResp resp = fileService.initPartUpload(dto);

            assertNotNull(resp);
            assertEquals(1, resp.getTotalChunks());
            assertEquals(5 * 1024 * 1024L, resp.getChunkSize());
        }

        @Test
        @DisplayName("不支持的文件后缀抛出异常")
        void initPartUpload_InvalidSuffix() {
            when(fileProperties.validSuffix("test.exe")).thenReturn(false);

            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("test.exe");
            dto.setFileSize(1024L);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.initPartUpload(dto));
            assertEquals("文件后缀不支持", exception.getMessage());
        }

        @Test
        @DisplayName("文件名包含路径遍历符号抛出异常")
        void initPartUpload_PathTraversal() {
            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("../test.pdf");
            dto.setFileSize(1024L);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.initPartUpload(dto));
            assertEquals("文件名不能含有特殊字符", exception.getMessage());
        }

        @Test
        @DisplayName("文件名包含 ./ 抛出异常")
        void initPartUpload_DotSlash() {
            FilePartDto.InitPartUploadDto dto = new FilePartDto.InitPartUploadDto();
            dto.setFileName("./test.pdf");
            dto.setFileSize(1024L);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.initPartUpload(dto));
            assertEquals("文件名不能含有特殊字符", exception.getMessage());
        }
    }

    // ==================== uploadPart 测试 ====================

    @Nested
    @DisplayName("uploadPart - 上传分片")
    class UploadPartTests {

        @Test
        @DisplayName("正常上传分片")
        void uploadPart_Success() throws IOException {
            String uploadId = UUID.randomUUID().toString();
            Integer partNumber = 1;
            MockMultipartFile file = new MockMultipartFile("file", "chunk1.dat",
                    "application/octet-stream", "test data".getBytes());

            when(filePartService.save(any(FilePart.class))).thenReturn(true);

            FilePartDto.UploadPartResp resp = fileService.uploadPart(uploadId, partNumber, file);

            assertNotNull(resp);
            assertEquals(partNumber, resp.getPartNumber());
            assertEquals(String.valueOf(partNumber), resp.getETag());
            verify(filePartService, times(1)).save(any(FilePart.class));
        }

        @Test
        @DisplayName("上传ID为空抛出异常")
        void uploadPart_NullUploadId() {
            MockMultipartFile file = new MockMultipartFile("file", "chunk1.dat",
                    "application/octet-stream", "test data".getBytes());

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.uploadPart(null, 1, file));
            assertEquals("上传ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("分片号为0抛出异常")
        void uploadPart_ZeroPartNumber() {
            MockMultipartFile file = new MockMultipartFile("file", "chunk1.dat",
                    "application/octet-stream", "test data".getBytes());

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.uploadPart("uploadId", 0, file));
            assertEquals("分片号必须大于0", exception.getMessage());
        }

        @Test
        @DisplayName("分片号为负数抛出异常")
        void uploadPart_NegativePartNumber() {
            MockMultipartFile file = new MockMultipartFile("file", "chunk1.dat",
                    "application/octet-stream", "test data".getBytes());

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.uploadPart("uploadId", -1, file));
            assertEquals("分片号必须大于0", exception.getMessage());
        }

        @Test
        @DisplayName("分片文件为空抛出异常")
        void uploadPart_EmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile("file", "chunk1.dat",
                    "application/octet-stream", new byte[0]);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.uploadPart("uploadId", 1, emptyFile));
            assertEquals("分片文件不能为空", exception.getMessage());
        }
    }

    // ==================== getUploadProgress 测试 ====================

    @Nested
    @DisplayName("getUploadProgress - 查询上传进度")
    class GetUploadProgressTests {

        @Test
        @DisplayName("有已上传分片时返回正确进度")
        void getUploadProgress_WithParts() {
            String uploadId = UUID.randomUUID().toString();

            FilePart part1 = new FilePart();
            part1.setUploadId(uploadId);
            part1.setPartNumber(1);
            part1.setETag("1");

            FilePart part2 = new FilePart();
            part2.setUploadId(uploadId);
            part2.setPartNumber(2);
            part2.setETag("2");

            when(filePartService.list(any(QueryWrapper.class))).thenReturn(Arrays.asList(part1, part2));

            FilePartDto.UploadProgressResp resp = fileService.getUploadProgress(uploadId);

            assertNotNull(resp);
            assertEquals(uploadId, resp.getUploadId());
            assertEquals(2, resp.getUploadedParts().size());
            assertFalse(resp.getIsComplete());
        }

        @Test
        @DisplayName("没有已上传分片时返回空列表")
        void getUploadProgress_NoParts() {
            String uploadId = UUID.randomUUID().toString();

            when(filePartService.list(any(QueryWrapper.class))).thenReturn(java.util.Collections.emptyList());

            FilePartDto.UploadProgressResp resp = fileService.getUploadProgress(uploadId);

            assertNotNull(resp);
            assertEquals(uploadId, resp.getUploadId());
            assertTrue(resp.getUploadedParts().isEmpty());
            assertFalse(resp.getIsComplete());
        }

        @Test
        @DisplayName("分片按编号升序排列")
        void getUploadProgress_PartsOrdered() {
            String uploadId = UUID.randomUUID().toString();

            FilePart part3 = new FilePart();
            part3.setUploadId(uploadId);
            part3.setPartNumber(3);
            part3.setETag("3");

            FilePart part1 = new FilePart();
            part1.setUploadId(uploadId);
            part1.setPartNumber(1);
            part1.setETag("1");

            FilePart part2 = new FilePart();
            part2.setUploadId(uploadId);
            part2.setPartNumber(2);
            part2.setETag("2");

            // 返回顺序是乱的
            when(filePartService.list(any(QueryWrapper.class))).thenReturn(Arrays.asList(part3, part1, part2));

            FilePartDto.UploadProgressResp resp = fileService.getUploadProgress(uploadId);

            assertNotNull(resp);
            assertEquals(3, resp.getUploadedParts().size());
            // 验证排序后的顺序
            assertEquals(1, resp.getUploadedParts().get(0).getPartNumber());
            assertEquals(2, resp.getUploadedParts().get(1).getPartNumber());
            assertEquals(3, resp.getUploadedParts().get(2).getPartNumber());
        }
    }

    // ==================== completePartUpload 测试 ====================

    @Nested
    @DisplayName("completePartUpload - 完成分片上传")
    class CompletePartUploadTests {

        @Test
        @DisplayName("上传ID为空抛出异常")
        void completePartUpload_NullUploadId() {
            FilePartDto.CompletePartUploadDto dto = new FilePartDto.CompletePartUploadDto();
            dto.setUploadId(null);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.completePartUpload(dto));
            assertEquals("上传ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("上传ID为空字符串抛出异常")
        void completePartUpload_EmptyUploadId() {
            FilePartDto.CompletePartUploadDto dto = new FilePartDto.CompletePartUploadDto();
            dto.setUploadId("");

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.completePartUpload(dto));
            assertEquals("上传ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("未找到上传分片抛出异常")
        void completePartUpload_NoPartsFound() {
            FilePartDto.CompletePartUploadDto dto = new FilePartDto.CompletePartUploadDto();
            dto.setUploadId("non-existent-upload-id");

            when(filePartService.list(any(QueryWrapper.class))).thenReturn(java.util.Collections.emptyList());

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.completePartUpload(dto));
            assertEquals("未找到上传分片", exception.getMessage());
        }
    }

    // ==================== abortPartUpload 测试 ====================

    @Nested
    @DisplayName("abortPartUpload - 取消分片上传")
    class AbortPartUploadTests {

        @Test
        @DisplayName("上传ID为空抛出异常")
        void abortPartUpload_NullUploadId() {
            FilePartDto.AbortPartUploadDto dto = new FilePartDto.AbortPartUploadDto();
            dto.setUploadId(null);

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.abortPartUpload(dto));
            assertEquals("上传ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("上传ID为空字符串抛出异常")
        void abortPartUpload_EmptyUploadId() {
            FilePartDto.AbortPartUploadDto dto = new FilePartDto.AbortPartUploadDto();
            dto.setUploadId("");

            BizException exception = assertThrows(BizException.class,
                    () -> fileService.abortPartUpload(dto));
            assertEquals("上传ID不能为空", exception.getMessage());
        }

        @Test
        @DisplayName("取消上传时删除分片记录")
        void abortPartUpload_DeletesFileParts() {
            FilePartDto.AbortPartUploadDto dto = new FilePartDto.AbortPartUploadDto();
            dto.setUploadId("test-upload-id");

            fileService.abortPartUpload(dto);

            verify(filePartService, times(1)).remove(any(QueryWrapper.class));
        }
    }

    // ==================== 辅助方法测试 ====================

    @Nested
    @DisplayName("辅助方法")
    class HelperMethodTests {

        @Test
        @DisplayName("sanitizeFileName 移除危险字符")
        void sanitizeFileName_RemovesDangerousChars() throws Exception {
            // 使用反射调用私有方法
            var method = FileServiceImpl.class.getDeclaredMethod("sanitizeFileName", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(fileService, "file:name?.pdf");
            assertEquals("file_name_.pdf", result);
        }

        @Test
        @DisplayName("sanitizeFileName 处理 null 输入")
        void sanitizeFileName_NullInput() throws Exception {
            var method = FileServiceImpl.class.getDeclaredMethod("sanitizeFileName", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(fileService, (String) null);
            assertEquals("unnamed", result);
        }

        @Test
        @DisplayName("getTempDir 返回正确的临时目录路径")
        void getTempDir_ReturnsCorrectPath() throws Exception {
            var method = FileServiceImpl.class.getDeclaredMethod("getTempDir", String.class);
            method.setAccessible(true);

            String uploadId = "test-upload-123";
            String tempDir = (String) method.invoke(fileService, uploadId);

            assertTrue(tempDir.contains("file-parts"));
            assertTrue(tempDir.contains(uploadId));
        }

        @Test
        @DisplayName("getDateFolder 返回正确格式的日期路径")
        void getDateFolder_ReturnsCorrectFormat() throws Exception {
            var method = FileServiceImpl.class.getDeclaredMethod("getDateFolder");
            method.setAccessible(true);

            String dateFolder = (String) method.invoke(fileService);

            // 格式应为 yyyy/MM/dd/
            assertTrue(dateFolder.matches("\\d{4}/\\d{2}/\\d{2}/"));
        }
    }
}
