package com.ajay.anime_app.rest;

import com.ajay.anime_app.model.UserDTO;
import com.ajay.anime_app.service.UserService;
import com.ajay.anime_app.util.ReferencedException;
import com.ajay.anime_app.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

    private final UserService userService;

    public UserResource(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(userService.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateUser(@RequestHeader("Authorization")  String token,
            @PathVariable(name = "id") final Long id,
                                           @RequestBody @Valid final UserDTO userDTO) {
        userService.update(id, userDTO,token);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") final Long id,
                                             @RequestHeader("Authorization") String token) {
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(id);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        userService.delete(id,token);
        return ResponseEntity.ok("User with userId" + id + "deleted successfully");
    }

}
