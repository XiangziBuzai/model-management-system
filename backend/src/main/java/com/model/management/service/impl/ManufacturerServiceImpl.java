package com.model.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.model.management.entity.Manufacturer;
import com.model.management.mapper.ManufacturerMapper;
import com.model.management.mapper.ModelMapper;
import com.model.management.service.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerMapper manufacturerMapper;
    private final ModelMapper modelMapper;

    @Override
    public List<Manufacturer> listAll() {
        // 获取所有厂家
        List<Manufacturer> allManufacturers = manufacturerMapper.selectList(
                new LambdaQueryWrapper<Manufacturer>().orderByAsc(Manufacturer::getName)
        );
        
        // 过滤出有模型关联的厂家
        return allManufacturers.stream()
                .filter(manufacturer -> hasModels(manufacturer.getId()))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查厂家是否有模型关联
     */
    private boolean hasModels(Long manufacturerId) {
        Long count = modelMapper.selectCount(
                new LambdaQueryWrapper<com.model.management.entity.Model>()
                        .eq(com.model.management.entity.Model::getManufacturerId, manufacturerId)
        );
        return count != null && count > 0;
    }

    @Override
    public Manufacturer create(String name, String description) {
        Manufacturer m = new Manufacturer();
        m.setName(name.trim());
        m.setDescription(description);
        manufacturerMapper.insert(m);
        return m;
    }

    @Override
    public Manufacturer update(Long id, String name, String description) {
        Manufacturer m = manufacturerMapper.selectById(id);
        if (m == null) throw new RuntimeException("厂家不存在");
        m.setName(name.trim());
        m.setDescription(description);
        manufacturerMapper.updateById(m);
        return m;
    }

    @Override
    public boolean delete(Long id) {
        return manufacturerMapper.deleteById(id) > 0;
    }
}