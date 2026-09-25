package com.resolveit.service;

import com.resolveit.dto.DepartmentDto;
import com.resolveit.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> findAllActive();

    List<Department> findAll();

    Department findById(Long id);

    Department createDepartment(DepartmentDto dto);

    Department updateDepartment(Long id, DepartmentDto dto);

    void toggleDepartmentStatus(Long id);
}
