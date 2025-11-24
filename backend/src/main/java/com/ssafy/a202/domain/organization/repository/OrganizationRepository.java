package com.ssafy.a202.domain.organization.repository;

import com.ssafy.a202.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
