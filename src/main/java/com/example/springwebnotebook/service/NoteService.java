package com.example.springwebnotebook.service;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.repository.NoteRepositoryStub;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepositoryStub noteRepository;

    public NoteService(NoteRepositoryStub noteRepository) {
        this.noteRepository = noteRepository;
        seedSample(); // optional sample data
    }

    // Returns all notes (copy)
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public Note createOrUpdateNote(Note note) {
        return noteRepository.save(note);
    }

    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public String getShareLink(Long id) {
        Optional<Note> note = noteRepository.findById(id);
        return note.map(n -> "/share/" + n.getShareKey()).orElse(null);
    }

    public Optional<Note> getNoteByShareKey(String shareKey) {
        return noteRepository.findByShareKey(shareKey);
    }

    // --- filtering + pagination helper ---
    public List<Note> findWithFilterAndPagination(Optional<String> titleFilter, int page, int size) {
        List<Note> base = getAllNotes();

        // filter by title if present
        if (titleFilter.isPresent() && !titleFilter.get().isBlank()) {
            String tf = titleFilter.get().toLowerCase();
            base = base.stream()
                    .filter(n -> n.getTitle() != null && n.getTitle().toLowerCase().contains(tf))
                    .collect(Collectors.toList());
        }

        // pagination (page: 0-based)
        int from = Math.max(0, page * size);
        if (from >= base.size()) return Collections.emptyList();
        int to = Math.min(base.size(), from + size);
        return base.subList(from, to);
    }

    private void seedSample() {
        // only if empty
        if (noteRepository.findAll().isEmpty()) {
            createOrUpdateNote(new Note(null, "First", "Content 1"));
            createOrUpdateNote(new Note(null, "Second", "Content 2"));
            createOrUpdateNote(new Note(null, "Shopping list", "Milk, Bread"));
            createOrUpdateNote(new Note(null, "Work plan", "Do stuff"));
        }
    }
}
