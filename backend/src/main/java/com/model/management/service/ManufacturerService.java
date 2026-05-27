package com.model.management.service;

import com.model.management.entity.Manufacturer;

import java.util.List;

public interface ManufacturerService {
    List<Manufacturer> listAll();
    Manufacturer create(String name, String description);
    Manufacturer update(Long id, String name, String description);
    boolean delete(Long id);
}
