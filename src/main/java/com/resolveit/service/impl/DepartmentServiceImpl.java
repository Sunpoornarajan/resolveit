package com.resolveit.service.impl;

import com.resolveit.dto.DepartmentDto;
import com.resolveit.entity.Department;
import com.resolveit.exception.DuplicateResourceException;
import com.resolveit.exception.ResourceNotFoundException;
import com.resolveit.repository.DepartmentRepository;
import com.resolveit.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> findAllActive() {
        return departmentRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    @Override
    public Department createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateResourceException("Department with name '" + dto.getName() + "' already exists");
        }
        Department department = new Department(dto.getName(), dto.getDescription());
        department.setActive(dto.isActive());
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, DepartmentDto dto) {
        Department department = findById(id);
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) {
            throw new DuplicateResourceException("Department with name '" + dto.getName() + "' already exists");
        }
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setActive(dto.isActive());
        return departmentRepository.save(department);
    }

    @Override
    public void toggleDepartmentStatus(Long id) {
        Department department = findById(id);
        department.setActive(!department.isActive());
        departmentRepository.save(department);
    }
}
