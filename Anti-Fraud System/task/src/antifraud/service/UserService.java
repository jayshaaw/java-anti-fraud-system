package antifraud.service;

import antifraud.AntiFraudApplication;
import antifraud.api.dto.*;
import antifraud.api.exception.BadRequest;
import antifraud.api.exception.InvalidRequest;
import antifraud.api.exception.NotFound;
import antifraud.api.exception.UnAuthorized;
import antifraud.model.Users;
import antifraud.repository.UserRepository;
import antifraud.security.AdminSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class UserService {


    private final UserRepository userRepository;

    private final AdminSecurity adminSecurity;


    public UserService(UserRepository userRepository, AdminSecurity adminSecurity) {
        this.userRepository = userRepository;
        this.adminSecurity = adminSecurity;
    }


    public ResponseEntity<RegUserResponse> registerUser(RegUserRequest regUserRequest) {
        String username = regUserRequest.getUsername();
        RegUserResponse registerUser = null;
        boolean usernameExists = userRepository.existsByUsernameIgnoreCase(username);
        synchronized (this) {
            if (userRepository.countFirstBy() == 0) {
                adminSecurity.setOperation("UNLOCK");
                adminSecurity.setRole("ADMINISTRATOR");
            } else {
                adminSecurity.setOperation("LOCK");
                adminSecurity.setRole("MERCHANT");
            }
        }

        if (usernameExists) {
            System.out.println("Name Exists!!!!");
            throw new InvalidRequest("Invalid Request");
        } else {
            Users savedUser = userRepository.save(new Users(regUserRequest.getName(), regUserRequest.getUsername(), AntiFraudApplication.getEncoder().encode((regUserRequest.getPassword())), adminSecurity.getRole(), adminSecurity.getOperation()));
            registerUser = RegUserResponse.builder().id(savedUser.getId()).name(savedUser.getName()).username(savedUser.getUsername()).role(adminSecurity.getRole()).build();

            return new ResponseEntity<>(registerUser, HttpStatus.CREATED);
        }
    }


    public List<UserListResponse> listUsers() {
        Iterable<Users> usersList = userRepository.findAll();
        List<UserListResponse> responseUserList = new ArrayList<>();

        for (Users user : usersList) {
            responseUserList.add(new UserListResponse(user.getId(), user.getName(), user.getUsername(), user.getRole()));
        }
        return responseUserList;
    }

    public UserDeleteResponse deleteUser(String username) {
        boolean usernameExists = userRepository.existsByUsernameIgnoreCase(username);
        if (!usernameExists) throw new NotFound("User not found!");
        if (!Objects.equals(username.toUpperCase(), "Administrator".toUpperCase())) {
            userRepository.deleteByUsernameIgnoreCase(username);
            return new UserDeleteResponse(username, "Deleted successfully!");
        } else {
            throw new UnAuthorized("Unauthorized request");
        }
    }

    public Users findUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public ResponseEntity<UserAccessResponse> modifyUserAccess(UserAccessRequest userAccessRequest) {
        boolean usernameExists = userRepository.existsByUsernameIgnoreCase(userAccessRequest.getUsername());
        if (!usernameExists) throw new NotFound("User not found!");

        if (!Objects.equals(userAccessRequest.getUsername().toUpperCase(), "Administrator".toUpperCase())) {
            System.out.println("User exists! and not an administrator");
            userRepository.updateOperationByUsernameIgnoreCase(userAccessRequest.getOperation(), userAccessRequest.getUsername());
            String msg = "User " + userAccessRequest.getUsername() + " " + userAccessRequest.getOperation().toLowerCase() + "ed!";
            return new ResponseEntity<>(UserAccessResponse.builder().status(msg).build(), HttpStatus.OK);
        } else {
            throw new BadRequest("Bad Request");
        }

    }

    public RegUserResponse userRoleDtoToUserResponse(Users users, String newRole) {
        return RegUserResponse.builder().id(users.getId()).name(users.getName()).username(users.getUsername()).role(newRole).build();
    }

    public ResponseEntity<RegUserResponse> modifyUserRole(UserRoleRequest userRoleRequest) {
        boolean usernameExists = userRepository.existsByUsernameIgnoreCase(userRoleRequest.getUsername());
        if (!usernameExists) throw new NotFound("User not found!");
        Users user = userRepository.findByUsernameIgnoreCase(userRoleRequest.getUsername());

        if (Objects.equals(user.getRole(), userRoleRequest.getRole()))
            throw new InvalidRequest("Conflict: User role same");

        if (!Objects.equals(userRoleRequest.getRole().toUpperCase(), "ADMINISTRATOR") && (Objects.equals(userRoleRequest.getRole().toUpperCase(), "SUPPORT") || Objects.equals(userRoleRequest.getRole().toUpperCase(), "MERCHANT"))) {
            userRepository.updateRoleByUsernameIgnoreCase(userRoleRequest.getRole(), userRoleRequest.getUsername());
            RegUserResponse userResponse = userRoleDtoToUserResponse(user, userRoleRequest.getRole());
            return new ResponseEntity<>(userResponse, HttpStatus.OK);
        } else {
            throw new BadRequest("Bad Request");
        }
    }

}
