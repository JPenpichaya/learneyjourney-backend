package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController implements MasterController<UserDto, String>{
    private final FirebaseAuthUtil firebaseAuthUtil;
    private final UserService userService;
    @Override
    public ResponseEntity<UserDto> getById(String s) {
        return ResponseEntity.ok(userService.getById(s));
    }

    @Override
    public ResponseEntity<Page<UserDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(userService.getAll(pageable));
    }

    @Override
    public ResponseEntity<UserDto> create(UserDto body) {
        return ResponseEntity.ok(userService.create(body));
    }

    @Override
    public ResponseEntity<UserDto> update(String s, UserDto body) {
        return ResponseEntity.ok(userService.update(s, body));
    }

    @Override
    public ResponseEntity<Void> delete(String s) {
        userService.deleteById(s);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/get-info")
    public ResponseEntity<UserDto> getUserInfo(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        return ResponseEntity.ok(userService.getById(firebaseAuthUtil.getUserIdFromToken(authHeader)));
    }

}
