package com.ukayukay.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ukayukay.backend.entity.BusinessRequest;
import com.ukayukay.backend.entity.User;

public interface BusinessRequestRepository extends JpaRepository<BusinessRequest, Integer> {
    List<BusinessRequest> findByUser(User user);
}
