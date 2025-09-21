package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class NoteRepositoryStub {

    private final List<Note> notes = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Note> findAll() {
        return new ArrayList<>(notes);
    }

    public Optional<Note> findById(Long id) {
        return notes.stream().filter(note -> note.getId().equals(id)).findFirst();
    }

    public Optional<Note> findByShareKey(String shareKey) {
        return notes.stream().filter(note -> note.getShareKey().equals(shareKey)).findFirst();
    }

    public Note save(Note note) {
        if (note.getId() == null) {
            note.setId(idCounter.getAndIncrement());
            notes.add(note);
        } else {
            notes.removeIf(n -> n.getId().equals(note.getId()));
            notes.add(note);
        }
        return note;
    }

    public void deleteById(Long id) {
        notes.removeIf(note -> note.getId().equals(id));
    }
}