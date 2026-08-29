package com.den.pulse.domain.member.repository;

import com.den.pulse.domain.member.entity.RoleMenuPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermission, UUID> {

    List<RoleMenuPermission> findByRole_IdIn(Collection<UUID> roleIds);
}
