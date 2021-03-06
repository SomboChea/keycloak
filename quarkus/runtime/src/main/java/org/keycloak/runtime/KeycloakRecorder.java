package org.keycloak.runtime;

import java.util.List;
import java.util.Map;

import io.quarkus.agroal.runtime.DataSourceSupport;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import org.keycloak.connections.liquibase.FastServiceLocator;
import org.keycloak.connections.liquibase.KeycloakLogger;

import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;

@Recorder
public class KeycloakRecorder {

    public static final SmallRyeConfig CONFIG;

    static {
        CONFIG = (SmallRyeConfig) SmallRyeConfigProviderResolver.instance().getConfig();
    }

    public static String getDatabaseDialect() {
        return CONFIG.getRawValue("quarkus.datasource.dialect");
    }

    public void configureLiquibase(Map<String, List<String>> services) {
        LogFactory.setInstance(new LogFactory() {
            KeycloakLogger logger = new KeycloakLogger();

            @Override
            public liquibase.logging.Logger getLog(String name) {
                return logger;
            }

            @Override
            public liquibase.logging.Logger getLog() {
                return logger;
            }
        });
        ServiceLocator.setInstance(new FastServiceLocator(services));
    }

    public BeanContainerListener configureDataSource() {
        return new BeanContainerListener() {
            @Override
            public void created(BeanContainer container) {
                String driver = CONFIG.getRawValue("quarkus.datasource.driver");
                DataSourceSupport instance = container.instance(DataSourceSupport.class);
                DataSourceSupport.Entry entry = instance.entries.get(DataSourceUtil.DEFAULT_DATASOURCE_NAME);
                entry.resolvedDriverClass = driver;
            }
        };
    }
}
