package com.den.pulse.domain.member.service;

import com.den.pulse.domain.member.entity.MenuKey;
import com.den.pulse.domain.member.entity.ProjectRole;
import com.den.pulse.domain.member.entity.RoleMenuPermission;
import com.den.pulse.domain.project.entity.Project;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 프로젝트 생성 시 관리자/편집자/뷰어 3개 기본 역할을 만든다.
 * DEN-DESIGN.md 5.5절 절충안(전역 기본 역할 + 프로젝트 예외) 중 지금 단계에서 채택한 단순화 버전:
 * PROJECT_ROLE.project_id는 nullable로 남겨두되, 전역 공유 역할(project=null)은 아직 쓰지 않고
 * 프로젝트마다 이 3개를 그대로 생성한다. 실제 커스텀 역할이 필요해지면 이 자리에서
 * 전역 기본 역할 상속 방식으로 전환한다.
 */
@Service
@RequiredArgsConstructor
public class ProjectRoleService {

    private record RoleTemplate(String name, boolean admin, Map<MenuKey, Boolean> menuPermissions) {
    }

    private static final List<RoleTemplate> DEFAULT_ROLE_TEMPLATES = List.of(
            new RoleTemplate("관리자", true, Map.of(MenuKey.TASKS, true, MenuKey.GANTT, true, MenuKey.MESSENGER, true)),
            new RoleTemplate("편집자", false, Map.of(MenuKey.TASKS, true, MenuKey.GANTT, true, MenuKey.MESSENGER, true)),
            new RoleTemplate("뷰어", false, Map.of(MenuKey.TASKS, true, MenuKey.GANTT, true, MenuKey.MESSENGER, false))
    );

    private final EntityManager entityManager;

    /** 3개 역할을 전부 생성하고, 요청자를 프로젝트 멤버로 등록할 때 쓸 관리자 역할을 반환한다. */
    public ProjectRole createDefaultRoles(Project project) {
        ProjectRole adminRole = null;
        for (RoleTemplate template : DEFAULT_ROLE_TEMPLATES) {
            ProjectRole role = new ProjectRole(project, template.name(), template.admin());
            entityManager.persist(role);
            for (Map.Entry<MenuKey, Boolean> entry : template.menuPermissions().entrySet()) {
                entityManager.persist(new RoleMenuPermission(role, entry.getKey(), entry.getValue()));
            }
            if (template.admin()) {
                adminRole = role;
            }
        }
        return adminRole;
    }
}
