package com.example.springwebnotebook.service;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.repository.NoteRepositoryStub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepositoryStub noteRepository;

    private String singletonBean;

    @Autowired // ін'єкція напряму
    private String prototypeBean;

    @Autowired // ін'єкція через конструктор
    public NoteService(NoteRepositoryStub noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Autowired // ін'єкція через setter
    public void setSingletonBean(String singletonBean) {
        this.singletonBean = singletonBean;
    }

    public List<Note> getAllNotes() {
        System.out.println(singletonBean);
        System.out.println(prototypeBean);
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
}