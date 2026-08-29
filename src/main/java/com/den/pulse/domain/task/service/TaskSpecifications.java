package com.den.pulse.domain.task.service;

import com.den.pulse.domain.task.entity.Task;
import com.den.pulse.domain.task.entity.TaskAssignee;
import com.den.pulse.domain.task.entity.TaskPriority;
import com.den.pulse.domain.task.entity.TaskStatus;
import com.den.pulse.domain.task.entity.TaskTag;
import com.den.pulse.domain.task.entity.TaskWatcher;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** GET /projects/{projectKey}/tasks 필터·비공개 가시성 조건을 DB 레벨에서 조립 (페이지네이션 정확도를 위해 후처리 필터링 대신 쿼리에 반영). */
final class TaskSpecifications {

    private TaskSpecifications() {
    }

    static Specification<Task> inProject(UUID projectId) {
        return (root, query, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    static Specification<Task> statusIn(List<TaskStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    static Specification<Task> priorityIn(List<TaskPriority> priorities) {
        return (root, query, cb) -> root.get("priority").in(priorities);
    }

    static Specification<Task> titleContains(String q) {
        String pattern = "%" + q.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    static Specification<Task> hasAnyAssignee(List<UUID> userIds) {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            var a = sub.from(TaskAssignee.class);
            sub.select(a.get("task").get("id")).where(a.get("user").get("id").in(userIds));
            return root.get("id").in(sub);
        };
    }

    static Specification<Task> hasAnyTag(List<UUID> tagIds) {
        return (root, query, cb) -> {
            Subquery<UUID> sub = query.subquery(UUID.class);
            var tt = sub.from(TaskTag.class);
            sub.select(tt.get("task").get("id")).where(tt.get("tag").get("id").in(tagIds));
            return root.get("id").in(sub);
        };
    }

    /** 비공개 업무는 담당자·참여자가 아니면 제외 (API-SPEC.md 0.4절). */
    static Specification<Task> visibleTo(UUID userId) {
        return (root, query, cb) -> {
            Subquery<Integer> assigneeSub = query.subquery(Integer.class);
            var a = assigneeSub.from(TaskAssignee.class);
            assigneeSub.select(cb.literal(1))
                    .where(cb.equal(a.get("task"), root), cb.equal(a.get("user").get("id"), userId));

            Subquery<Integer> watcherSub = query.subquery(Integer.class);
            var w = watcherSub.from(TaskWatcher.class);
            watcherSub.select(cb.literal(1))
                    .where(cb.equal(w.get("task"), root), cb.equal(w.get("user").get("id"), userId));

            return cb.or(
                    cb.isFalse(root.get("isPrivate")),
                    cb.exists(assigneeSub),
                    cb.exists(watcherSub)
            );
        };
    }
}
