package com.example.springwebnotebook.controller;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.service.IService;
import com.example.springwebnotebook.service.NoteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes API", description = "CRUD + filter/pagination + patch examples for Note resource")
public class NoteRestController {

    private final IService noteService;
    private final ObjectMapper objectMapper;

    public NoteRestController(IService noteService, ObjectMapper objectMapper) {
        this.noteService = noteService;
        this.objectMapper = objectMapper;
    }

    // ---------------- GET all (filter + pagination) ----------------
    @Operation(summary = "List notes (supports filter and pagination)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of notes returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Note.class)))),
            @ApiResponse(responseCode = "400", description = "Bad request (invalid pagination parameters)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> listNotes(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        if (page < 0 || size <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid pagination parameters: page must be >= 0, size > 0"));
        }

        List<Note> items = noteService.findWithFilterAndPagination(Optional.ofNullable(title), page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", page);
        resp.put("size", size);
        resp.put("items", items);
        return ResponseEntity.ok(resp);
    }

    // ---------------- GET by ID ----------------
    @Operation(summary = "Get note by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note found",
                    content = @Content(schema = @Schema(implementation = Note.class))),
            @ApiResponse(responseCode = "400", description = "Invalid id format"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        // parse id with validation -> NumberFormatException handled by ApiExceptionHandler
        long noteId = Long.parseLong(id);

        return noteService.getNoteById(noteId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found with id = " + id)));
    }

    // ---------------- CREATE ----------------
    @Operation(summary = "Create new note")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Note.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Note note) {
        if (note == null ||
            note.getTitle() == null ||
            note.getTitle().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Title is required"));
        }
        Number created = noteService.createNote(note);
        return created.intValue() != 0 ?
                ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", "id of created note " + created))
                :
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "note hasn't been created"));
    }

    // ---------------- UPDATE (PUT — full) ----------------
    @Operation(summary = "Replace note (full update)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Replaced", content = @Content(schema = @Schema(implementation = Note.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> replace(@PathVariable String id, @RequestBody Note newNote) {
        long noteId = Long.parseLong(id);
        if (newNote == null ||
            newNote.getTitle() == null ||
            newNote.getTitle().isBlank()
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Title is required"));
        }

        Optional<Note> existing = noteService.getNoteById(noteId);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Note not found with id = " + id));
        }

        Optional<Note> saved = noteService.updateNote(newNote);

        if(saved.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Couldn't update this note" + id));
        return ResponseEntity.ok(saved);
    }

    // ---------------- PATCH (JSON Patch, RFC 6902) ----------------
    @Operation(summary = "Patch note (JSON Patch RFC6902)", description = "Use Content-Type: application/json-patch+json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patched (JSON Patch)", content = @Content(schema = @Schema(implementation = Note.class))),
            @ApiResponse(responseCode = "400", description = "Invalid patch document"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<?> patchWithJsonPatch(@PathVariable String id, @RequestBody JsonPatch patch) {
        long noteId = Long.parseLong(id);

        Optional<Note> existingOpt = noteService.getNoteById(noteId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Note not found with id = " + id));
        }

        try {
            Note existing = existingOpt.get();
            JsonNode node = objectMapper().convertValue(existing, JsonNode.class);
            JsonNode patched = patch.apply(node);
            Note patchedNote = objectMapper().treeToValue(patched, Note.class);
            patchedNote.setId(noteId);
            Optional<Note> saved = noteService.updateNote(patchedNote);
            if(saved.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Couldn't update this note" + id));
            return ResponseEntity.ok(saved);
        } catch (JsonPatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid JSON Patch: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    // ---------------- PATCH (JSON Merge Patch, RFC 7386) ----------------
    @Operation(summary = "Patch note (JSON Merge Patch RFC7386)", description = "Use Content-Type: application/merge-patch+json")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patched (Merge Patch)", content = @Content(schema = @Schema(implementation = Note.class))),
            @ApiResponse(responseCode = "400", description = "Invalid merge patch"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping(path = "/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<?> patchWithMergePatch(@PathVariable String id, @RequestBody JsonNode mergePatch) {
        long noteId = Long.parseLong(id);

        Optional<Note> existingOpt = noteService.getNoteById(noteId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Note not found with id = " + id));
        }

        try {
            Note existing = existingOpt.get();
            // use ObjectMapper.readerForUpdating to apply merge patch semantics
            Note patched = objectMapper().readerForUpdating(existing).readValue(mergePatch);
            patched.setId(noteId);
            Optional<Note> saved = noteService.updateNote(patched);
            if(saved.isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Couldn't update this note" + id));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid merge patch: " + e.getMessage()));
        }
    }

    // ---------------- DELETE ----------------
    @Operation(summary = "Delete a note by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        long noteId = Long.parseLong(id);

        Optional<Note> existing = noteService.getNoteById(noteId);
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Note not found with id = " + id));
        }
        noteService.deleteNote(noteId);
        return ResponseEntity.noContent().build();
    }

    // helper to get a fresh ObjectMapper (you can also inject one via constructor if preferred)
    private ObjectMapper objectMapper() {
        return this.objectMapper;
    }
}
