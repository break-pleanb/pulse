package com.den.pulse.core.jpa;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.converter.spi.BasicValueConverter;
import org.hibernate.type.descriptor.java.EnumJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * Hibernate 6.2+는 {@code @Enumerated(EnumType.STRING)} 컬럼에 "값 목록 CHECK 제약"을 DDL 생성 시
 * 자동으로 붙인다({@link EnumJavaType#getCheckCondition}). 문제는 {@code ddl-auto: update}가 기존
 * 제약을 enum 값이 늘어나도 갱신하지 않는다는 것 — enum에 값을 추가할 때마다
 * "violates check constraint ..._check" 에러가 재발한다 (2026-08-30, TaskActivity.field에
 * DEPENDENCIES 추가하며 실제로 발생).
 * <p>
 * {@link #getCheckCondition}만 오버라이드해 null을 반환하고, 나머지 name() 기반 직렬화 동작은
 * {@link EnumJavaType} 그대로 쓴다 — 이 프로젝트가 ddl-auto:update로 스키마를 관리하는 한
 * (CLAUDE.md, Flyway 전환 전) 모든 STRING enum 컬럼에 이 방식을 써야 재발하지 않는다.
 * 사용법: enum마다 no-arg 생성자를 가진 구체 서브클래스를 만들고(예: TaskStatusJavaType),
 * 엔티티 필드에 {@code @org.hibernate.annotations.JavaType(TaskStatusJavaType.class)}를 붙인다.
 */
public abstract class NoCheckEnumJavaType<T extends Enum<T>> extends EnumJavaType<T> {

    protected NoCheckEnumJavaType(Class<T> type) {
        super(type);
    }

    @Override
    public String getCheckCondition(String columnName, JdbcType jdbcType, BasicValueConverter<T, ?> converter, Dialect dialect) {
        return null;
    }
}
