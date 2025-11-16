package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class NoteLinksRepository implements INoteLinksDAO {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NotesRowMapper noteRowMapper = new NotesRowMapper();

    @Autowired
    public NoteLinksRepository(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("notes_to_notes");
    }

    @Override
    public List<Note> findLinkedFromNote(Long id) {
        String FIND_LINKED_NOTES = "select notes.* from notes_to_notes nn JOIN notes ON notes.id = nn.linked_id where nn.linker_id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("id", id);

        return this.namedParameterJdbcTemplate
                .query(FIND_LINKED_NOTES, namedParameters, noteRowMapper);
    }

    @Override
    public void deleteLinkFromNote(Long linked_id, Long linker_id) {
        String DELETE_LINK_TO_NOTE = "DELETE FROM notes_to_notes WHERE linked_id = :linked_id AND linker_id = :linker_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("linked_id", linked_id)
                .addValue("linker_id", linker_id);

        this.namedParameterJdbcTemplate
                .update(DELETE_LINK_TO_NOTE, namedParameters);
    }

    @Override
    public void addLinkToNote(Long linkerId, Long linkedId) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("linker_id", linkerId);
        parameters.put("linked_id", linkedId);

        this.simpleJdbcInsert.execute(parameters);
    }
}
