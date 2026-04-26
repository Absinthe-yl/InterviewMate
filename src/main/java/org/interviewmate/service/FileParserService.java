package org.interviewmate.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileParserService {
    /**
     * 解析文件内容为文本
     * @param file 上传的文件
     * @return 文件文本内容
     */
    String parseFile(MultipartFile file);
}
