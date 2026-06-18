package com.grim.contextos.tag;

import com.grim.contextos.auth.model.Role;
import com.grim.contextos.auth.model.UserPrincipal;
import com.grim.contextos.auth.security.CustomUserDetailsService;
import com.grim.contextos.auth.security.JwtTokenProvider;
import com.grim.contextos.tag.controller.TagController;
import com.grim.contextos.tag.dto.request.CreateTagRequest;
import com.grim.contextos.tag.dto.request.UpdateTagRequest;
import com.grim.contextos.tag.dto.response.TagResponse;
import com.grim.contextos.tag.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID tagId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final TagResponse tagResponse = new TagResponse(tagId, "fiction", "#ff0000", ownerId);
    private final UserPrincipal principal = new UserPrincipal(ownerId, "test@test.com", "hash", Role.USER);

    @Test
    void createTagReturns201() throws Exception {
        when(tagService.createTag(any(CreateTagRequest.class), any())).thenReturn(tagResponse);

        mockMvc.perform(post("/api/v1/tags")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"fiction","color":"#ff0000"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("fiction"));
    }

    @Test
    void createTagReturns400WhenNameMissing() throws Exception {
        mockMvc.perform(post("/api/v1/tags")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"color":"#ff0000"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listTagsReturns200() throws Exception {
        when(tagService.listTags(any())).thenReturn(List.of(tagResponse));

        mockMvc.perform(get("/api/v1/tags")
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("fiction"));
    }

    @Test
    void getTagReturns200() throws Exception {
        when(tagService.getTag(tagId)).thenReturn(tagResponse);

        mockMvc.perform(get("/api/v1/tags/{id}", tagId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(tagId.toString()));
    }

    @Test
    void updateTagReturns200() throws Exception {
        var updated = new TagResponse(tagId, "non-fiction", "#0000ff", ownerId);
        when(tagService.updateTag(eq(tagId), any(UpdateTagRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/tags/{id}", tagId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"non-fiction","color":"#0000ff"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("non-fiction"))
            .andExpect(jsonPath("$.data.color").value("#0000ff"));
    }

    @Test
    void deleteTagReturns200() throws Exception {
        mockMvc.perform(delete("/api/v1/tags/{id}", tagId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void assignTagsReturns200() throws Exception {
        UUID containerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/tags/assign")
                .with(user(principal))
                .param("containerId", containerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"" + tagId + "\"]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void removeTagReturns200() throws Exception {
        UUID containerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tags/{tagId}/containers/{containerId}", tagId, containerId)
                .with(user(principal))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchTagsReturns200() throws Exception {
        when(tagService.searchTags(eq("fict"), any())).thenReturn(List.of(tagResponse));

        mockMvc.perform(get("/api/v1/tags/search")
                .with(user(principal))
                .param("q", "fict")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("fiction"));
    }

    @Test
    void autocompleteTagsReturns200() throws Exception {
        when(tagService.autocompleteTags(eq("fic"), any())).thenReturn(List.of(tagResponse));

        mockMvc.perform(get("/api/v1/tags/autocomplete")
                .with(user(principal))
                .param("q", "fic")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("fiction"));
    }
}
