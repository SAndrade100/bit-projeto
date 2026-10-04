package info.bitsolucoes.solicitacoes.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import info.bitsolucoes.solicitacoes.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthApiTest {

    @Autowired MockMvc mvc;

    @Test
    void rotasProtegidasExigemLogin() throws Exception {
        mvc.perform(get("/api/solicitacoes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginValidoCriaSessaoEPermiteAcesso() throws Exception {
        MockHttpSession sessao = login("ana.silva", "senha123");

        mvc.perform(get("/api/auth/me").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana.silva"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
        mvc.perform(get("/api/dashboard").session(sessao)).andExpect(status().isOk());
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana.silva\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Usuário ou senha inválidos."));
    }

    @Test
    void loginSemCamposRetorna400() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.username").exists())
                .andExpect(jsonPath("$.erros.password").exists());
    }

    @Test
    void loginComCaractereNuloNoUsuarioRetorna400EmVezDeErroDeBanco() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana\\u0000\",\"password\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginSemTokenCsrfEhRejeitado() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ana.silva\",\"password\":\"senha123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutEncerraASessao() throws Exception {
        MockHttpSession sessao = login("ana.silva", "senha123");

        mvc.perform(post("/api/auth/logout").with(csrf()).session(sessao)).andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").session(sessao)).andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String username, String senha) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, senha)))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
