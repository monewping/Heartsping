// =================== [임시용] UserServiceImpl ===================
package org.project.monewping.domain.user;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.user.entity.User;
import org.project.monewping.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(String email, String nickname, String password) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .build();
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
}
// =================== [임시용 끝] =================== 