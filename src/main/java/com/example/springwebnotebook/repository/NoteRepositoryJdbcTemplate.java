package com.example.springwebnotebook.repository;

import com.example.springwebnotebook.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class NoteRepositoryJdbcTemplate implements INotesDAO {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NotesRowMapper noteRowMapper = new NotesRowMapper();

    @Autowired
    public NoteRepositoryJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("Notes")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Note> findAll() {
        String SELECT_ALL_NOTES = "select * from Notes";
        List<Note> notes = List.of();

        try{
            notes = this.jdbcTemplate.query(SELECT_ALL_NOTES, noteRowMapper);
        } catch(EmptyResultDataAccessException e){
            System.out.println("No notes found");
        }

        return notes;
    }

    @Override
    public Optional<Note> findById(Number id) {
        String SELECT_NOTE_WITH_ID = "select * from Notes where id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("id", id);
        Note note = null;

        try{
            note = this.namedParameterJdbcTemplate
                    .queryForObject(SELECT_NOTE_WITH_ID, namedParameters, new BeanPropertyRowMapper<>(Note.class));
        } catch(EmptyResultDataAccessException e){
            System.out.println("No notes found");
        }

        return Optional.ofNullable(note);
    }

    @Override
    public Optional<Note> findByShareKey(String shareKey) {
        String SELECT_NOTE_WITH_SHARE_KEY = "select * from `Notes` where share_key = :shareKey";
        SqlParameterSource namedParameters = new MapSqlParameterSource("shareKey", shareKey);
        Note note = null;

        try{
            note = this.namedParameterJdbcTemplate
                    .queryForObject(SELECT_NOTE_WITH_SHARE_KEY, namedParameters, new BeanPropertyRowMapper<>(Note.class));
        } catch(EmptyResultDataAccessException e){
            System.out.println("No notes found");
        }

        return Optional.ofNullable(note);
    }

    @Override
    public Long create(Note note) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("title", note.getTitle());
        parameters.put("content", note.getContent());
        parameters.put("share_key", note.getShareKey());

        return this.simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public Note update(Note note) {
        String UPDATE_NOTE = "update Notes set title = :title, content = :content where id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("title", note.getTitle())
                .addValue("content", note.getContent())
                .addValue("id", note.getId());

        try{
            this.namedParameterJdbcTemplate.update(UPDATE_NOTE, namedParameters);
        } catch(EmptyResultDataAccessException e){
            System.out.println("No notes found");
        }
        return this.findById(note.getId()).orElse(null);
    }

    @Override
    public void delete(Long id) {
        String DELETE_NOTE = "delete from Notes where id = :id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("id", id);

        try{
            this.namedParameterJdbcTemplate.update(DELETE_NOTE, namedParameters);
        } catch(EmptyResultDataAccessException e){
            System.out.println("No notes found");
        }
    }
}