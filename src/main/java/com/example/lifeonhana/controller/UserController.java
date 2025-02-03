package com.example.lifeonhana.controller;

import com.example.lifeonhana.ApiResult;
import com.example.lifeonhana.dto.response.UserResponseDTO;
import com.example.lifeonhana.dto.response.MyDataResponseDTO;
import com.example.lifeonhana.dto.response.UserNicknameResponseDTO;
import com.example.lifeonhana.service.UserService;
import com.example.lifeonhana.global.exception.BaseException;
import com.example.lifeonhana.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/info")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResult<UserResponseDTO>> getUserInfo(
        @AuthenticationPrincipal String authId
    ) {
        if (authId == null || authId.isEmpty()) {
            throw new BaseException(ErrorCode.AUTH_REQUIRED);
        }

        UserResponseDTO response = userService.getUserInfo(authId);
        return ResponseEntity.ok(
            ApiResult.success(ErrorCode.USER_INFO_SUCCESS, response)
        );
    }

    @Operation(summary = "마이데이터 조회", description = "사용자의 마이데이터 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "마이데이터를 찾을 수 없습니다.")
    })
    @GetMapping("/mydata")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResult<MyDataResponseDTO>> getMyData(
        @AuthenticationPrincipal String authId
    ) {
        if (authId == null || authId.isEmpty()) {
            throw new BaseException(ErrorCode.AUTH_REQUIRED);
        }

        MyDataResponseDTO response = userService.getMyData(authId);
        return ResponseEntity.ok(
            ApiResult.success(ErrorCode.MYDATA_INFO_SUCCESS, response)
        );
    }

    @Operation(summary = "사용자 칭호 조회", description = "사용자의 관심사 기반 맞춤 칭호를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증이 필요합니다."),
        @ApiResponse(responseCode = "404", description = "좋아요 기록이 없습니다.")
    })
    @GetMapping("/nickname")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResult<UserNicknameResponseDTO>> getUserNickname(
        @AuthenticationPrincipal String authId
    ) {
        if (authId == null || authId.isEmpty()) {
            throw new BaseException(ErrorCode.AUTH_REQUIRED);
        }

        UserNicknameResponseDTO response = userService.getUserNickname(authId);
        return ResponseEntity.ok(
            ApiResult.success(ErrorCode.NICKNAME_INFO_SUCCESS, response)
        );
    }
}