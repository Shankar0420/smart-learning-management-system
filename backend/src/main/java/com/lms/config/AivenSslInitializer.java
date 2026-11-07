package com.lms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Initializes and materializes the Aiven Cloud MariaDB CA certificate from the application
 * classpath onto the filesystem before HikariCP and Hibernate establish database connections.
 *
 * The MariaDB Connector/J driver requires 'serverSslCert' to point to an accessible filesystem path
 * or literal PEM content, and cannot directly resolve 'classpath:...' URLs inside an executable Spring Boot JAR.
 *
 * This initializer:
 * 1. Extracts 'aiven-ca.pem' from classpath to '/tmp/aiven-ca.pem', java.io.tmpdir, and './aiven-ca.pem'.
 * 2. Also writes './classpath.pem' to safely catch legacy 'serverSslCert=classpath.pem' URLs.
 * 3. Rewrites any 'serverSslCert=classpath...' or 'serverSslCert=classpath.pem' references in
 *    spring.datasource.url to the verified absolute filesystem path.
 */
public class AivenSslInitializer implements EnvironmentPostProcessor, ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AivenSslInitializer.class);
    private static final String CERT_RESOURCE = "/aiven-ca.pem";
    private static final String DEFAULT_TMP_PATH = "/tmp/aiven-ca.pem";

    private static volatile String materializedCertPath = null;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String certPath = extractCertificates();
        normalizeDatasourceUrl(environment, certPath);
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        String certPath = extractCertificates();
        normalizeDatasourceUrl(applicationContext.getEnvironment(), certPath);
    }

    public static synchronized String init() {
        return extractCertificates();
    }

    public static synchronized String extractCertificates() {
        if (materializedCertPath != null && new File(materializedCertPath).exists()) {
            return materializedCertPath;
        }

        try (InputStream in = AivenSslInitializer.class.getResourceAsStream(CERT_RESOURCE)) {
            if (in == null) {
                log.warn("[AivenSslInitializer] Resource '{}' not found on classpath", CERT_RESOURCE);
                return null;
            }

            byte[] certBytes = in.readAllBytes();

            // Destination 1: Standard /tmp/aiven-ca.pem (Linux / Render standard)
            File primaryTarget = new File(DEFAULT_TMP_PATH);
            tryWriteFile(primaryTarget, certBytes);

            // Destination 2: System temp dir (cross-platform fallback)
            File sysTempTarget = new File(System.getProperty("java.io.tmpdir"), "aiven-ca.pem");
            tryWriteFile(sysTempTarget, certBytes);

            // Destination 3: Current working directory (./aiven-ca.pem)
            File localTarget = new File("aiven-ca.pem");
            tryWriteFile(localTarget, certBytes);

            // Destination 4: Legacy alias (./classpath.pem)
            File legacyAlias = new File("classpath.pem");
            tryWriteFile(legacyAlias, certBytes);

            if (primaryTarget.exists() && primaryTarget.length() > 0) {
                materializedCertPath = primaryTarget.getAbsolutePath();
            } else if (sysTempTarget.exists() && sysTempTarget.length() > 0) {
                materializedCertPath = sysTempTarget.getAbsolutePath();
            } else if (localTarget.exists() && localTarget.length() > 0) {
                materializedCertPath = localTarget.getAbsolutePath();
            }

            log.info("[AivenSslInitializer] Aiven CA certificate materialized successfully at: {}", materializedCertPath);
            return materializedCertPath;
        } catch (Exception e) {
            log.error("[AivenSslInitializer] Failed to extract Aiven CA certificate: {}", e.getMessage(), e);
            return null;
        }
    }

    private static void tryWriteFile(File file, byte[] data) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data);
                fos.flush();
            }
        } catch (Exception ignored) {
            // best-effort write for supplementary fallback locations
        }
    }

    private static void normalizeDatasourceUrl(ConfigurableEnvironment environment, String certPath) {
        if (environment == null) {
            return;
        }

        String url = environment.getProperty("spring.datasource.url");
        if (url == null || url.isBlank()) {
            return;
        }

        String effectiveCertPath = (certPath != null) ? certPath : DEFAULT_TMP_PATH;

        // Check if URL contains problematic classpath references for serverSslCert
        if (url.contains("serverSslCert=classpath.pem") ||
            url.contains("serverSslCert=classpath:aiven-ca.pem") ||
            url.contains("serverSslCert=classpath:") ||
            url.contains("serverSslCert=classpath.cer")) {

            String normalizedUrl = url
                    .replace("serverSslCert=classpath:aiven-ca.pem", "serverSslCert=" + effectiveCertPath)
                    .replace("serverSslCert=classpath.pem", "serverSslCert=" + effectiveCertPath)
                    .replace("serverSslCert=classpath.cer", "serverSslCert=" + effectiveCertPath);

            // Generic fallback for any remaining classpath: prefix in serverSslCert
            if (normalizedUrl.contains("serverSslCert=classpath:")) {
                normalizedUrl = normalizedUrl.replaceAll("serverSslCert=classpath:[^&]*", "serverSslCert=" + effectiveCertPath);
            }

            log.info("[AivenSslInitializer] Normalized JDBC URL serverSslCert reference to: {}", effectiveCertPath);

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", normalizedUrl);
            environment.getPropertySources().addFirst(new MapPropertySource("aivenSslUrlOverride", props));
        }
    }
}

