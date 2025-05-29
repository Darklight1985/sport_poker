package ru.poker.sportpoker.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.model.TypeContributor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.springframework.context.annotation.Configuration;
import ru.poker.sportpoker.domain.JsonSetUUIDType;

import java.util.Set;
import java.util.UUID;

@Configuration
public class HibernateTypeConfig implements TypeContributor {

    @Override
    public void contribute(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        // Регистрируем пользовательский тип
        JsonSetUUIDType jsonSetUUIDType = new JsonSetUUIDType();
        JavaType<Set<UUID>> javaType = typeContributions.getTypeConfiguration()
                .getJavaTypeRegistry()
                .resolveDescriptor(Set.class);

        typeContributions.getTypeConfiguration()
                .getJdbcTypeRegistry()
                .addDescriptor((JdbcType) jsonSetUUIDType);

        typeContributions.getTypeConfiguration()
                .getJavaTypeRegistry()
                .addDescriptor(javaType);
    }
}
