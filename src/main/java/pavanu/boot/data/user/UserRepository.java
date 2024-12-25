package pavanu.boot.data.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Pavan Uppuluri
 */
interface UserRepository extends JpaRepository<User, Long> {
}
