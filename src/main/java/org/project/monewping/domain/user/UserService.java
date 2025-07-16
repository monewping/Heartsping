// =================== [임시용] UserService ===================
package org.project.monewping.domain.user;

import org.project.monewping.domain.user.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(String email, String nickname, String password);
    Optional<User> findById(UUID id);
}
// =================== [임시용 끝] =================== 