package org.springframework.web.multipart.commons;


import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommonsMultipartFile implements MultipartFile, Serializable {
    protected static final Log logger = LogFactory.getLog(CommonsMultipartFile.class);
    private final FileItem fileItem;
    private final long size;
    private boolean preserveFilename = false;

    public CommonsMultipartFile(FileItem fileItem) {
        this.fileItem = fileItem;
        this.size = this.fileItem.getSize();
    }

    public final FileItem getFileItem() {
        return this.fileItem;
    }

    public void setPreserveFilename(boolean preserveFilename) {
        this.preserveFilename = preserveFilename;
    }

    public String getName() {
        return this.fileItem.getFieldName();
    }

    public String getOriginalFilename() {
        String filename = this.fileItem.getName();
        if (filename == null) {
            return "";
        } else if (this.preserveFilename) {
            return filename;
        } else {
            int unixSep = filename.lastIndexOf(47);
            int winSep = filename.lastIndexOf(92);
            int pos = Math.max(winSep, unixSep);
            return pos != -1 ? filename.substring(pos + 1) : filename;
        }
    }

    public String getContentType() {
        return this.fileItem.getContentType();
    }

    public boolean isEmpty() {
        return this.size == 0L;
    }

    public long getSize() {
        return this.size;
    }

    public byte[] getBytes() {
        if (!this.isAvailable()) {
            throw new IllegalStateException("File has been moved - cannot be read again");
        } else {
            byte[] bytes = this.fileItem.get();
            return bytes != null ? bytes : new byte[0];
        }
    }

    public InputStream getInputStream() throws IOException {
        if (!this.isAvailable()) {
            throw new IllegalStateException("File has been moved - cannot be read again");
        } else {
            InputStream inputStream = this.fileItem.getInputStream();
            return inputStream != null ? inputStream : StreamUtils.emptyInput();
        }
    }

    public void transferTo(File dest) throws IOException, IllegalStateException {
        if (!this.isAvailable()) {
            throw new IllegalStateException("File has already been moved - cannot be transferred again");
        } else if (dest.exists() && !dest.delete()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted");
        } else {
            try {
                this.fileItem.write(dest.toPath());
                LogFormatUtils.traceDebug(logger, (traceOn) -> {
                    String action = "transferred";
                    if (!this.fileItem.isInMemory()) {
                        action = this.isAvailable() ? "copied" : "moved";
                    }

                    return "Part '" + this.getName() + "',  filename '" + this.getOriginalFilename() + "'" + (traceOn ? ", stored " + this.getStorageDescription() : "") + ": " + action + " to [" + dest.getAbsolutePath() + "]";
                });
            } catch (FileUploadException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            } catch (IOException | IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IOException("File transfer failed", ex);
            }
        }
    }

    public void transferTo(Path dest) throws IOException, IllegalStateException {
        if (!this.isAvailable()) {
            throw new IllegalStateException("File has already been moved - cannot be transferred again");
        } else {
            FileCopyUtils.copy(this.fileItem.getInputStream(), Files.newOutputStream(dest));
        }
    }

    protected boolean isAvailable() {
        if (this.fileItem.isInMemory()) {
            return true;
        } else if (this.fileItem instanceof DiskFileItem) {
            Path path = ((DiskFileItem) this.fileItem).getPath();
//            return ((DiskFileItem) this.fileItem).getStoreLocation().exists();
            return path != null && path.toFile().exists();
        } else {
            return this.fileItem.getSize() == this.size;
        }
    }

    public String getStorageDescription() {
        if (this.fileItem.isInMemory()) {
            return "in memory";
        } else {
            return this.fileItem instanceof DiskFileItem ? "at [" + ((DiskFileItem) this.fileItem).getPath().toAbsolutePath() + "]" : "on disk";
        }
    }

    public String toString() {
        return "MultipartFile[field=\"" + this.fileItem.getFieldName() + "\"" + (this.fileItem.getName() != null ? ", filename=" + this.fileItem.getName() : "") + (this.fileItem.getContentType() != null ? ", contentType=" + this.fileItem.getContentType() : "") + ", size=" + this.fileItem.getSize() + "]";
    }
}