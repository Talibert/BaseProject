package com.example.api_docker.infra.controller.user;

import com.example.api_docker.ControllerAbstractTests;
import com.example.api_docker.application.user.result.UserResult;
import com.example.api_docker.application.user.usecase.ChangeUserPasswordUseCase;
import com.example.api_docker.application.user.usecase.CreateUserUseCase;
import com.example.api_docker.application.user.usecase.GetUserUseCase;
import com.example.api_docker.application.user.usecase.ListUsersUseCase;
import com.example.api_docker.domain.shared.pagination.PageResult;
import com.example.api_docker.infra.controller.user.request.ChangePasswordRequest;
import com.example.api_docker.infra.controller.user.request.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerAbstractTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private ListUsersUseCase listUsersUseCase;

    @MockitoBean
    private ChangeUserPasswordUseCase changeUserPasswordUseCase;

    @Test
    void shouldReturn201WhenCreatingUserWithValidData() throws Exception {
        CreateUserRequest request = getCreateUserRequest();

        doNothing().when(createUserUseCase).execute(any());

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + tokenDoUser)) // ← token ADMIN
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn401WhenCreatingUserWithoutToken() throws Exception {
        CreateUserRequest request = getCreateUserRequest();

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WhenGettingMe() throws Exception {
        UserResult userResult = new UserResult(
                idDoUser.value(), "Guilherme Taliberti",
                "guilhermetaliberti@gmail.com", LocalDateTime.now()
        );

        when(getUserUseCase.execute(any())).thenReturn(userResult);

        mockMvc.perform(get("/user/me")
                        .header("Authorization", "Bearer " + tokenDoUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Guilherme Taliberti"));
    }

    @Test
    void shouldReturn200WhenGettingUserById() throws Exception {
        UserResult userResult = new UserResult(
                idDoUser.value(), "Guilherme Taliberti",
                "guilhermetaliberti@email.com",  LocalDateTime.now()
        );

        when(getUserUseCase.execute(any())).thenReturn(userResult);

        mockMvc.perform(get("/user/{id}", idDoUser.value())
                        .header("Authorization", "Bearer " + tokenDoUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Guilherme Taliberti"));
    }

    @Test
    void shouldReturn204WhenChangingPasswordWithValidData() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("senhaAtual123", "novaSenha123");

        doNothing().when(changeUserPasswordUseCase).execute(any());

        mockMvc.perform(patch("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + tokenDoUser))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn200WhenListingUsersPaginated() throws Exception {
        UserResult userResult = new UserResult(
                idDoUser.value(), "Guilherme Taliberti",
                "guilhermetaliberti@gmail.com", LocalDateTime.now()
        );
        PageResult<UserResult> pageResult = new PageResult<>(
                List.of(userResult),
                0,
                10,
                1L,
                1
        );

        when(listUsersUseCase.execute(any())).thenReturn(pageResult);

        mockMvc.perform(get("/user")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortDirection", "DESC")
                        .header("Authorization", "Bearer " + tokenDoUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Guilherme Taliberti"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.isFirst").value(true))
                .andExpect(jsonPath("$.isLast").value(true));
    }

    @Test
    void shouldReturn401WhenChangingPasswordWithoutToken() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("senhaAtual123", "novaSenha123");

        mockMvc.perform(patch("/user/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    private static CreateUserRequest getCreateUserRequest() {
        return new CreateUserRequest(
                "Guilherme", "Taliberti",
                "guilhermenovo@gmail.com", "senha123"
        );
    }
}
