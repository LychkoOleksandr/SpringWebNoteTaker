package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;

import java.util.List;

public interface INoteLinksDAO {
    List<Note> findLinkedFromNote(Long id);
    void deleteLinkFromNote(Long linked_id, Long linker_id);
    void addLinkToNote(Long linkerId, Long linkedId);
}
