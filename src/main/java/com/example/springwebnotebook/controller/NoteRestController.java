package com.example.springwebnotebook.controller;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.service.NoteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes API", description = "REST API для роботи з нотатками")
public class NoteRestController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- CRUD ----------

    @Operation(summary = "Отримати всі нотатки", description = "Повертає всі нотатки з можливістю фільтрації та пагінації.")
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        List<Note> notes = noteService.getAllNotes();

        if (title != null && !title.isEmpty()) {
            notes = notes.stream()
                    .filter(n -> n.getTitle() != null && n.getTitle().toLowerCase().contains(title.toLowerCase()))
                    .collect(Collectors.toList());
        }

        int fromIndex = Math.min(page * size, notes.size());
        int toIndex = Math.min(fromIndex + size, notes.size());
        List<Note> paginated = notes.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paginated);
    }

    @Operation(summary = "Отримати нотатку за ID")
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Створити нову нотатку")
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        Note created = noteService.createOrUpdateNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Оновити нотатку повністю")
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id, @RequestBody Note note) {
        if (noteService.getNoteById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        note.setId(id);
        Note updated = noteService.createOrUpdateNote(note);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Видалити нотатку")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (noteService.getNoteById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- PATCH (часткове оновлення) ----------

    @Operation(summary = "Часткове оновлення нотатки (RFC 6902)", description = "JSON Patch — список операцій add, remove, replace, etc.")
    @PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<Note> patchNote(@PathVariable Long id, @RequestBody JsonPatch patch) {
        Optional<Note> noteOpt = noteService.getNoteById(id);
        if (noteOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            Note note = noteOpt.get();
            JsonNode patched = patch.apply(objectMapper.convertValue(note, JsonNode.class));
            Note updated = objectMapper.treeToValue(patched, Note.class);
            updated.setId(id);
            noteService.createOrUpdateNote(updated);
            return ResponseEntity.ok(updated);
        } catch (JsonPatchException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Часткове оновлення нотатки (RFC 7386)", description = "JSON Merge Patch — передайте тільки поля, які треба змінити.")
    @PatchMapping(value = "/merge/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Note> mergePatchNote(@PathVariable Long id, @RequestBody JsonNode patch) {
        Optional<Note> noteOpt = noteService.getNoteById(id);
        if (noteOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            JsonNode existing = objectMapper.convertValue(noteOpt.get(), JsonNode.class);
            JsonNode merged = objectMapper.readerForUpdating(existing).readValue(patch);
            Note updated = objectMapper.treeToValue(merged, Note.class);
            updated.setId(id);
            noteService.createOrUpdateNote(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
