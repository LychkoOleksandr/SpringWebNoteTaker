package com.example.springwebnotebook.service;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.repository.INoteLinksDAO;
import com.example.springwebnotebook.repository.INotesDAO;
import com.example.springwebnotebook.repository.LinkPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@EnableTransactionManagement
public class NoteService implements IService {

    private final INotesDAO noteRepository;
    private final INoteLinksDAO noteLinksRepository;

    @Autowired
    public NoteService(@Qualifier("noteRepositoryJdbcClient") INotesDAO noteRepository, INoteLinksDAO noteLinksRepository) {
        this.noteRepository = noteRepository;
        this.noteLinksRepository = noteLinksRepository;
    }

    // Returns all notes (copy)
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) { return Optional.empty(); }

        List<LinkPair> linkPairs = noteLinksRepository
                .findLinkedFromNote(note.getId())
                .stream()
                .map(n -> new LinkPair(n.getId(), n.getTitle()))
                .toList();

        note.setLinks(linkPairs);
        return Optional.of(note);
    }

    public Optional<Note> getNoteByShareKey(String shareKey) {
        return noteRepository.findByShareKey(shareKey);
    }

    @Transactional
    public Number createNote(Note note) {
        Long noteId = noteRepository.create(note).longValue();

        if (note.getLinks() == null) return noteId;

        for (LinkPair linkPair : note.getLinks()) {
            noteLinksRepository.addLinkToNote(noteId, linkPair.getId());
        }

        return noteId;
    }

    @Transactional
    public Optional<Note> updateNote(Note note) {
        noteRepository.update(note);
        if (note.getLinks() == null) return noteRepository.findById(note.getId());

        Set<Long> existingIds = noteLinksRepository
                .findLinkedFromNote(note.getId())
                .stream()
                .map(Note::getId)
                .collect(Collectors.toSet());

        for (LinkPair linkPair : note.getLinks()) {
            Long newId = linkPair.getId();

            if (!existingIds.contains(newId)) noteLinksRepository.addLinkToNote(note.getId(), newId);

            existingIds.remove(newId);
        }

        if (existingIds.isEmpty()) return noteRepository.findById(note.getId());

        for (Long id : existingIds) noteLinksRepository.deleteLinkFromNote(id, note.getId());

        return noteRepository.findById(note.getId());
    }

    @Transactional
    public void deleteNote(Long id) {
        noteRepository.delete(id);
    }

    public String getShareLink(Long id) {
        Optional<Note> note = noteRepository.findById(id);
        return note.map(n -> "/share/" + n.getShareKey()).orElse(null);
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
}
