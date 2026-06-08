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
    
    /**
     * 批量设置模型为公开
     */
    boolean batchSetPublic(List<Long> ids);
    
    /**
     * 批量设置模型为私有
     */
    boolean batchSetPrivate(List<Long> ids);
    
    /**
     * 分页查询公开模型列表（广场使用）
     * @param sortBy 排序方式：newest-最新（默认），hot-最热
     * @param manufacturerIds 厂家ID列表（可选）
     */
    PageResult<ModelVO> getPublicModels(int pageNum, int pageSize, String keyword, String sortBy, List<Long> manufacturerIds);
    
    /**
     * 获取公开模型详情（广场使用）
     */
    ModelVO getPublicModelById(Long id);
    
    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);

    /**
     * 分页查询指定用户的公开模型列表
     */
    PageResult<ModelVO> getPublicModelsByUser(Long userId, int pageNum, int pageSize);
    
    /**
     * 设置当前用户所有模型为公开
     */
    boolean setAllPublic();
    
    /**
     * 设置当前用户所有模型为私有
     */
    boolean setAllPrivate();
}
