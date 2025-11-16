package com.example.springwebnotebook.model;

import com.example.springwebnotebook.repository.LinkPair;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.UUID;

public class Note {

    private Long id;
    private String title;
    private String content;
    private String shareKey;
    private List<LinkPair> links;

    public Note() {
        this.shareKey = UUID.randomUUID().toString();
    };

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getShareKey() {
        return shareKey;
    }

    public void setShareKey(String shareKey) {
        this.shareKey = shareKey;
    }

    public List<LinkPair> getLinks() {
        return links;
    }

    public void setLinks(List<LinkPair> links) {
        this.links = links;
    }
}