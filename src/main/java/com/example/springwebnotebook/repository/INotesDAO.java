package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;

import java.util.List;
import java.util.Optional;

public interface INotesDAO{
    List<Note> findAll();
    Optional<Note> findById(Number id);
    Optional<Note> findByShareKey(String author);
    Number create(Note note);
    Note update(Note note);
    void delete(Long id);
}
