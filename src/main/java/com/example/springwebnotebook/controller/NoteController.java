package com.example.springwebnotebook.controller;

import com.example.springwebnotebook.model.Note;
import com.example.springwebnotebook.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @GetMapping
    public String listNotes(Model model) {
        model.addAttribute("notes", noteService.getAllNotes());
        return "notes";
    }

    @GetMapping("/new")
    public String newNote(Model model) {
        model.addAttribute("note", new Note());
        return "note-form";
    }

    @GetMapping("/edit/{id}")
    public String editNote(@PathVariable Long id, Model model) {
        noteService.getNoteById(id).ifPresent(note -> model.addAttribute("note", note));
        return "note-form";
    }

    @PostMapping("/save")
    public String saveNote(@ModelAttribute Note note) {
        noteService.createOrUpdateNote(note);
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return "redirect:/";
    }

    @GetMapping("/share-link/{id}")
    public String getShareLink(@PathVariable Long id, Model model) {
        String link = noteService.getShareLink(id);
        model.addAttribute("shareLink", link);
        return "share-link";
    }

    @GetMapping("/share/{shareKey}")
    public String viewSharedNote(@PathVariable String shareKey, Model model) {
        noteService.getNoteByShareKey(shareKey).ifPresent(note -> model.addAttribute("note", note));
        return "shared-note";
    }
}