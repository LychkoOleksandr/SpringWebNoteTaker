package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NotesRowMapper implements RowMapper<Note> {
    @Override
    public Note mapRow(ResultSet rs, int rowNum) throws SQLException {
        Note note = new Note();

        note.setId(rs.getLong("id"));
        note.setTitle(rs.getString("title"));
        note.setContent(rs.getString("content"));
        note.setShareKey(rs.getString("share_key"));

        return note;
    }
}
