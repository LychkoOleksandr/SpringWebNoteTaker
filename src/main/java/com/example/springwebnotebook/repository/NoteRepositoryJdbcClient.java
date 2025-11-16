package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class NoteRepositoryJdbcClient implements INotesDAO {

    private final JdbcClient jdbcClient;
    private final NotesRowMapper noteRowMapper = new NotesRowMapper();

    @Autowired
    public NoteRepositoryJdbcClient(DataSource dataSource) {
        this.jdbcClient = JdbcClient.create(dataSource);
    }

    @Override
    public List<Note> findAll() {
        String SELECT_ALL_NOTES = "SELECT * FROM notes";

        return jdbcClient.sql(SELECT_ALL_NOTES)
                .query(noteRowMapper)
                .list();
    }

    @Override
    public Optional<Note> findById(Number id) {
        String SELECT_NOTES_WITH_ID = "SELECT * FROM Notes WHERE id = :id";

        return jdbcClient.sql(SELECT_NOTES_WITH_ID)
                .param("id", id)
                .query(noteRowMapper)
                .optional();
    }

    @Override
    public Optional<Note> findByShareKey(String shareKey) {
        String SELECT_NOTES_WITH_ID = "SELECT * FROM Notes WHERE share_key = :shareKey";

        return jdbcClient.sql(SELECT_NOTES_WITH_ID)
                .param("shareKey", shareKey)
                .query(noteRowMapper)
                .optional();
    }

    @Override
    public Number create(Note note) {
        String CREATE_NOTE = """
                INSERT INTO Notes (title, content, share_key)
                VALUES (:title, :content, :shareKey)
                """;
        KeyHolder KeyHolder = new GeneratedKeyHolder();

        return jdbcClient.sql(CREATE_NOTE)
                .params(Map.of(
                        "title", note.getTitle(),
                        "content", note.getContent(),
                        "shareKey", note.getShareKey()
                ))
                .update(KeyHolder);
    }

    @Override
    public Note update(Note note) {
        String UPDATE_NOTE = """
                    UPDATE Notes
                    SET title = :title,
                        content = :content
                    WHERE id = :id
                """;
        jdbcClient.sql(UPDATE_NOTE)
                .params(Map.of(
                        "title", note.getTitle(),
                        "content", note.getContent(),
                        "id", note.getId()
                ))
                .update();

        Optional<Note> updated = findById(note.getId());
        return updated.orElse(null);
    }

    @Override
    public void delete(Long id) {
        String DELETE_NOTE = "DELETE FROM Notes WHERE id = :id";
        jdbcClient.sql(DELETE_NOTE)
                .param("id", id)
                .update();
    }
}
