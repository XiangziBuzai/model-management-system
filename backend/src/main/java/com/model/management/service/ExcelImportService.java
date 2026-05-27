package com.model.management.service;

import com.model.management.vo.ImportResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelImportService {
    ImportResultVO importModels(MultipartFile file);
    ImportResultVO importTools(MultipartFile file);
}
