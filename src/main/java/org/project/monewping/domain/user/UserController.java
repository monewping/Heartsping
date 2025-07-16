// =================== [임시용] UserController ===================
package org.project.monewping.domain.user;

import lombok.RequiredArgsConstructor;
import org.project.monewping.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestParam String email,
                                           @RequestParam String nickname,
                                           @RequestParam String password) {
        User user = userService.createUser(email, nickname, password);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
// =================== [임시용 끝] =================== 