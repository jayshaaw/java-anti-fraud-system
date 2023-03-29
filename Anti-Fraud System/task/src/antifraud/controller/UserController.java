package antifraud.controller;

import antifraud.api.dto.*;
import antifraud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String sayHello() {
        return "Hello endpoint!";
    }

    @PostMapping("/user")
    public ResponseEntity<RegUserResponse> registerUser(@RequestBody @NotNull @Valid RegUserRequest regUserRequest) {
        return userService.registerUser(regUserRequest);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserListResponse>> listUsers() {
        return new ResponseEntity<>(userService.listUsers(), HttpStatus.OK);
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable String username) {
        return new ResponseEntity<>(userService.deleteUser(username), HttpStatus.OK);
    }

    @PutMapping("/access")
    public ResponseEntity<UserAccessResponse> modifyUserAccess(@RequestBody UserAccessRequest userAccessRequest) {
        return userService.modifyUserAccess(userAccessRequest);
    }

    @PutMapping("/role")
    public ResponseEntity<RegUserResponse> modifyUserRole(@RequestBody UserRoleRequest userRoleRequest) {
        return userService.modifyUserRole(userRoleRequest);
    }

}
