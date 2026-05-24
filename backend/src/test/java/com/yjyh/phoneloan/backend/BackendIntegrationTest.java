package com.yjyh.phoneloan.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendIntegrationTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void coreBorrowReturnAndEventFlowWorks() throws Exception {
        String ownerToken = login("10086", "password123").accessToken();
        String inviteCode = ownerCreateInvite(ownerToken);
        Auth user = register("20001", "张三", "password123", inviteCode);

        mvc.perform(post("/api/devices")
                .header("Authorization", bearer(user.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"测试手机","imei1":"123456789012345"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.imei1").value("123456789012345"))
            .andExpect(jsonPath("$.owner.employeeNo").value("20001"));

        mvc.perform(get("/api/devices/by-imei/123456789012345"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("测试手机"));

        String secondInvite = ownerCreateInvite(ownerToken);
        Auth borrower = register("20002", "李四", "password123", secondInvite);
        MvcResult borrowResult = mvc.perform(post("/api/loans/borrow-by-imei")
                .header("Authorization", bearer(borrower.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"imei":"123456789012345"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.device.currentHolder.employeeNo").value("20002"))
            .andReturn();

        String loanId = objectMapper.readTree(borrowResult.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(get("/api/loans/active")
                .header("Authorization", bearer(user.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(loanId))
            .andExpect(jsonPath("$[0].device.currentHolder.employeeNo").value("20002"));

        mvc.perform(get("/api/loans/active")
                .header("Authorization", bearer(borrower.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(loanId))
            .andExpect(jsonPath("$[0].device.owner.employeeNo").value("20001"));

        mvc.perform(post("/api/loans/" + loanId + "/return")
                .header("Authorization", bearer(borrower.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RETURNED"));

        mvc.perform(post("/api/events")
                .header("Authorization", bearer(borrower.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sessionId":"s-test",
                      "eventName":"borrow_confirm_result",
                      "screen":"scanBorrow",
                      "action":"confirmBorrow",
                      "result":"SUCCESS",
                      "severity":"INFO",
                      "context":{"imei":"123****2345"},
                      "appVersion":"0.5-test",
                      "deviceModel":"emulator",
                      "osVersion":"test"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isString());
    }

    @Test
    void invalidImeiIsRejected() throws Exception {
        mvc.perform(get("/api/devices/by-imei/123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("IMEI_INVALID"));
    }

    private Auth login(String employeeNo, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"employeeNo":"%s","password":"%s"}
                    """.formatted(employeeNo, password)))
            .andExpect(status().isOk())
            .andReturn();
        return authFrom(result);
    }

    private Auth register(String employeeNo, String name, String password, String inviteCode) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"employeeNo":"%s","name":"%s","password":"%s","inviteCode":"%s"}
                    """.formatted(employeeNo, name, password, inviteCode)))
            .andExpect(status().isOk())
            .andReturn();
        return authFrom(result);
    }

    private String ownerCreateInvite(String ownerToken) throws Exception {
        MvcResult result = mvc.perform(post("/api/owner/invite-codes")
                .header("Authorization", bearer(ownerToken)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(node.get("code").asText()).isNotBlank();
        return node.get("code").asText();
    }

    private Auth authFrom(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Auth(node.get("accessToken").asText(), node.get("refreshToken").asText());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Auth(String accessToken, String refreshToken) {}
}
