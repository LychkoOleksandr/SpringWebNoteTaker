package com.example.springwebnotebook.service;

import com.example.springwebnotebook.model.Note;
import java.util.List;
import java.util.Optional;

public interface IService {
    List<Note> getAllNotes();
    Optional<Note> getNoteById(Long id);
    Optional<Note> getNoteByShareKey(String shareKey);
    Number createNote(Note note);
    Optional<Note> updateNote(Note note);
    void deleteNote(Long id);
    List<Note> findWithFilterAndPagination(Optional<String> title, int page, int size);
}
