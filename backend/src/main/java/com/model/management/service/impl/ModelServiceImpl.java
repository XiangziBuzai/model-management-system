package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.model.management.common.PageResult;
import com.model.management.dto.ModelQueryDTO;
import com.model.management.dto.ModelSaveDTO;
import com.model.management.entity.Model;
import com.model.management.mapper.ModelMapper;
import com.model.management.service.ModelService;
import com.model.management.vo.ModelVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final ModelMapper modelMapper;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute("userId");
        }
        return null;
    }

    @Override
    public PageResult<ModelVO> page(ModelQueryDTO dto) {
        // 设置当前用户ID，只查询该用户的数据
        Long userId = getCurrentUserId();
        if (userId != null) {
            dto.setUserId(userId);
        }
        
        Page<ModelVO> page = new Page<>(dto.getPage(), dto.getSize());
        modelMapper.selectModelVOPage(page, dto);
        return PageResult.of(page);
    }

    @Override
    public ModelVO getById(Long id) {
        Long userId = getCurrentUserId();
        Model model = modelMapper.selectOne(
                new LambdaQueryWrapper<Model>()
                        .eq(Model::getId, id)
                        .eq(userId != null, Model::getUserId, userId)
        );
        if (model == null) return null;
        ModelVO vo = new ModelVO();
        vo.setId(model.getId());
        vo.setManufacturerId(model.getManufacturerId());
        vo.setName(model.getName());
        vo.setPrice(model.getPrice());
        vo.setRemark(model.getRemark());
        vo.setCover(model.getCover());
        vo.setSold(model.getSold());
        vo.setIsPublic(model.getIsPublic());
        vo.setCreatedAt(model.getCreatedAt());
        vo.setUpdatedAt(model.getUpdatedAt());
        return vo;
    }

    @Override
    public ModelVO create(ModelSaveDTO dto) {
        Long userId = getCurrentUserId();
        Model model = new Model();
        model.setManufacturerId(dto.getManufacturerId());
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setRemark(dto.getRemark());
        model.setCover(dto.getCover());
        model.setSold(dto.getSold() != null ? dto.getSold() : 0);
        model.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);
        model.setUserId(userId); // 设置用户ID
        modelMapper.insert(model);
        return getById(model.getId());
    }

    @Override
    public ModelVO update(Long id, ModelSaveDTO dto) {
        Long userId = getCurrentUserId();
        Model model = modelMapper.selectOne(
                new LambdaQueryWrapper<Model>()
                        .eq(Model::getId, id)
                        .eq(userId != null, Model::getUserId, userId)
        );
        if (model == null) throw new RuntimeException("模型不存在或无权限操作");
        model.setManufacturerId(dto.getManufacturerId());
        model.setName(dto.getName().trim());
        model.setPrice(dto.getPrice());
        model.setRemark(dto.getRemark());
        model.setCover(dto.getCover());
        model.setSold(dto.getSold() != null ? dto.getSold() : 0);
        model.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 0);
        modelMapper.updateById(model);
        return getById(id);
    }

    @Override
    public boolean delete(Long id) {
        Long userId = getCurrentUserId();
        Model model = modelMapper.selectOne(
                new LambdaQueryWrapper<Model>()
                        .eq(Model::getId, id)
                        .eq(userId != null, Model::getUserId, userId)
        );
        if (model == null) return false;
        return modelMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        Long userId = getCurrentUserId();
        if (userId == null || ids == null || ids.isEmpty()) {
            return false;
        }
        // 批量删除时，确保只能删除当前用户的数据
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<Model>()
                .in(Model::getId, ids)
                .eq(Model::getUserId, userId);
        return modelMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean batchSetPublic(List<Long> ids) {
        Long userId = getCurrentUserId();
        if (userId == null || ids == null || ids.isEmpty()) {
            return false;
        }
        // 批量设置公开时，确保只能操作当前用户的数据
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<Model>()
                .in(Model::getId, ids)
                .eq(Model::getUserId, userId);
        
        Model model = new Model();
        model.setIsPublic(1);
        return modelMapper.update(model, wrapper) > 0;
    }

    @Override
    public boolean batchSetPrivate(List<Long> ids) {
        Long userId = getCurrentUserId();
        if (userId == null || ids == null || ids.isEmpty()) {
            return false;
        }
        // 批量设置私有时，确保只能操作当前用户的数据
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<Model>()
                .in(Model::getId, ids)
                .eq(Model::getUserId, userId);
        
        Model model = new Model();
        model.setIsPublic(0);
        return modelMapper.update(model, wrapper) > 0;
    }

    @Override
    public PageResult<ModelVO> getPublicModels(int pageNum, int pageSize, String keyword, String sortBy, List<Long> manufacturerIds) {
        Page<ModelVO> page = new Page<>(pageNum, pageSize);
        modelMapper.selectPublicModelVOPage(page, keyword, sortBy, manufacturerIds);
        return PageResult.of(page);
    }

    @Override
    public ModelVO getPublicModelById(Long id) {
        return modelMapper.selectPublicModelVOById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        modelMapper.incrementViewCount(id);
    }

    @Override
    public PageResult<ModelVO> getPublicModelsByUser(Long userId, int pageNum, int pageSize) {
        Page<ModelVO> page = new Page<>(pageNum, pageSize);
        modelMapper.selectPublicModelVOPageByUser(page, userId);
        return PageResult.of(page);
    }

    @Override
    public boolean setAllPublic() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<Model>()
                .eq(Model::getUserId, userId);
        
        Model model = new Model();
        model.setIsPublic(1);
        return modelMapper.update(model, wrapper) > 0;
    }

    @Override
    public boolean setAllPrivate() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<Model>()
                .eq(Model::getUserId, userId);
        
        Model model = new Model();
        model.setIsPublic(0);
        return modelMapper.update(model, wrapper) > 0;
    }
}