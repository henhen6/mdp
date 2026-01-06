package top.mddata.gateway.sop.util;

import com.gitee.sop.support.dto.FileData;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 六如
 */
public class ResponseUtil {

    public static void writerFile(FileData fileData, HttpServletResponse response) throws IOException {
        InputStream inputStream = fileData.getInputStream();
        String contentType = fileData.getContentType();
        if (contentType != null) {
            response.setContentType(contentType);
        }
        response.setHeader("Content-disposition", "attachment; filename=" + fileData.getOriginalFilename());
        response.setHeader("Content-Length", String.valueOf(fileData.getSize()));
        IOUtils.copy(inputStream, response.getOutputStream());
    }


}
