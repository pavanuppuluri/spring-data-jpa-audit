package pavanu.boot.data.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pavan Uppuluri
 */
@Testcontainers
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop", includeFilters = @Filter(EnableJpaAuditing.class))
class UserAuditTests {

    @Container
    @ServiceConnection
    private final static MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:latest");

    @Autowired
    private UserRepository repository;

    @Test
    @DisplayName("When a user is saved Then created and modified fields are set And createdBy and modifiedBy fields are set to Mr. Auditor")
    void create() {
        var user = new User("Pavan Uppuluri", "pavanu");

        var createdUser = repository.save(user);

        assertThat(createdUser).extracting("created", "modified").isNotNull();
        assertThat(createdUser).extracting("createdBy", "modifiedBy").containsOnly("Mr. Auditor");
    }

    @Test
    @DisplayName("When a user is updated Then modified field should be updated")
    @Sql(statements = "INSERT INTO users (id, name, username, created, modified) VALUES ('84', 'Pavan Uppuluri', 'pavanu', now() - INTERVAL 7 DAY, now() - INTERVAL 7 DAY)")
    void update() {
        var modifiedUser = repository.findById(84L).map(user -> { user.setUsername("pavan.uppuluri"); return user; }).map(repository::saveAndFlush).orElseThrow();

        var created = (Instant) ReflectionTestUtils.getField(modifiedUser, "created");
        var modified = (Instant) ReflectionTestUtils.getField(modifiedUser, "modified");

        assertThat(modified).isAfter(created);
    }
}
