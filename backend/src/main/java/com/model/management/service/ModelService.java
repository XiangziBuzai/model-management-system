package com.model.management.service;

import com.model.management.common.PageResult;
import com.model.management.dto.ModelQueryDTO;
import com.model.management.dto.ModelSaveDTO;
import com.model.management.vo.ModelVO;

import java.util.List;

public interface ModelService {
    PageResult<ModelVO> page(ModelQueryDTO dto);
    ModelVO getById(Long id);
    ModelVO create(ModelSaveDTO dto);
    ModelVO update(Long id, ModelSaveDTO dto);
    boolean delete(Long id);
    boolean batchDelete(List<Long> ids);
}
