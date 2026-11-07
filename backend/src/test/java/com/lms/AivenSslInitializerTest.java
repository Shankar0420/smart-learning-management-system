package com.lms;

import com.lms.config.AivenSslInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Aiven SSL Certificate Initializer & URL Normalizer Tests")
class AivenSslInitializerTest {

    @Test
    @DisplayName("Should extract aiven-ca.pem to filesystem on startup")
    void shouldExtractAivenCaCertificateToFileSystem() throws Exception {
        String path = AivenSslInitializer.extractCertificates();
        assertNotNull(path, "Extracted certificate path should not be null");

        File file = new File(path);
        assertTrue(file.exists(), "Extracted certificate file must exist on filesystem");
        assertTrue(file.length() > 0, "Extracted certificate file must not be empty");

        String content = Files.readString(file.toPath());
        assertTrue(content.contains("BEGIN CERTIFICATE"), "Must contain valid PEM header");
        assertTrue(content.contains("END CERTIFICATE"), "Must contain valid PEM footer");
    }

    @Test
    @DisplayName("Should normalize DB_URL containing classpath: reference to filesystem path")
    void shouldNormalizeClasspathUrlToFilesystemPath() {
        AivenSslInitializer initializer = new AivenSslInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url",
                "jdbc:mariadb://mysql-aiven.com:25708/lms_db?sslMode=verify-full&serverSslCert=classpath:aiven-ca.pem");

        initializer.postProcessEnvironment(env, new SpringApplication());

        String updatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(updatedUrl);
        assertFalse(updatedUrl.contains("classpath:aiven-ca.pem"), "Must replace classpath: reference");
        assertTrue(updatedUrl.contains("serverSslCert=/tmp/aiven-ca.pem") || updatedUrl.contains("aiven-ca.pem"),
                "Must point to materialized filesystem path");
    }

    @Test
    @DisplayName("Should normalize DB_URL containing classpath.pem alias to filesystem path")
    void shouldNormalizeClasspathPemAliasToFilesystemPath() {
        AivenSslInitializer initializer = new AivenSslInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.datasource.url",
                "jdbc:mariadb://mysql-aiven.com:25708/lms_db?sslMode=verify-full&serverSslCert=classpath.pem");

        initializer.postProcessEnvironment(env, new SpringApplication());

        String updatedUrl = env.getProperty("spring.datasource.url");
        assertNotNull(updatedUrl);
        assertFalse(updatedUrl.contains("classpath.pem"), "Must replace classpath.pem alias");
        assertTrue(updatedUrl.contains("serverSslCert=/tmp/aiven-ca.pem") || updatedUrl.contains("aiven-ca.pem"),
                "Must point to materialized filesystem path");
    }

    @Test
    @DisplayName("Should preserve clean filesystem DB_URL unchanged")
    void shouldPreserveCleanFilesystemUrl() {
        AivenSslInitializer initializer = new AivenSslInitializer();
        MockEnvironment env = new MockEnvironment();
        String originalUrl = "jdbc:mariadb://mysql-aiven.com:25708/lms_db?sslMode=verify-full&serverSslCert=/tmp/aiven-ca.pem";
        env.setProperty("spring.datasource.url", originalUrl);

        initializer.postProcessEnvironment(env, new SpringApplication());

        String updatedUrl = env.getProperty("spring.datasource.url");
        assertEquals(originalUrl, updatedUrl, "Direct filesystem paths should remain unchanged");
    }
}

