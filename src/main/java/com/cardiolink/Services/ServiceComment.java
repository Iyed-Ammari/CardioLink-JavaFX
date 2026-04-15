package com.cardiolink.Services;

import com.cardiolink.Models.Comment;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment implements Iservice<Comment> {

    private Connection cnx;

    public ServiceComment() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    // ================= ADD =================
    @Override
    public void add(Comment comment) throws SQLDataException {

        try {

            String query = "INSERT INTO comment (content, created_at, post_id, user_id) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setString(1, comment.getContent());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, comment.getPost_id());
            ps.setInt(4, comment.getUser_id());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    // ================= UPDATE (ONLY CONTENT) =================
    @Override
    public void update(Comment c) throws SQLDataException {

        try {

            Comment existing = getById(c.getId());

            if (existing != null && existing.getUser_id() == c.getUser_id()) {

                String query = "UPDATE comment SET content=? WHERE id=?";

                PreparedStatement ps = cnx.prepareStatement(query);

                ps.setString(1, c.getContent());
                ps.setInt(2, c.getId());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    // ================= DELETE (SECURE OWNER ONLY) =================
    @Override
    public void delete(Comment c) throws SQLDataException {

        try {

            Comment existing = getById(c.getId());

            if (existing != null && existing.getUser_id() == c.getUser_id()) {

                String query = "DELETE FROM comment WHERE id=?";

                PreparedStatement ps = cnx.prepareStatement(query);
                ps.setInt(1, c.getId());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    // ================= GET BY ID =================
    @Override
    public Comment getById(int id) throws SQLDataException {

        Comment c = null;

        try {

            String query = "SELECT * FROM comment WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                c = new Comment();

                c.setId(rs.getInt("id"));
                c.setContent(rs.getString("content"));
                c.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                c.setPost_id(rs.getInt("post_id"));
                c.setUser_id(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return c;
    }

    // ================= GET ALL =================
    @Override
    public List<Comment> getAll() throws SQLDataException {

        List<Comment> comments = new ArrayList<>();

        try {

            String query = "SELECT * FROM comment";

            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {

                Comment c = new Comment();

                c.setId(rs.getInt("id"));
                c.setContent(rs.getString("content"));
                c.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                c.setPost_id(rs.getInt("post_id"));
                c.setUser_id(rs.getInt("user_id"));

                comments.add(c);
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return comments;
    }

    // ================= GET BY POST =================
    public List<Comment> getByPostId(int postId) throws SQLDataException {

        List<Comment> comments = new ArrayList<>();

        try {

            String query = "SELECT * FROM comment WHERE post_id=? ORDER BY created_at DESC";

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, postId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Comment c = new Comment();

                c.setId(rs.getInt("id"));
                c.setContent(rs.getString("content"));
                c.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                c.setPost_id(rs.getInt("post_id"));
                c.setUser_id(rs.getInt("user_id"));

                comments.add(c);
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return comments;
    }

    // ================= SORT =================
    public List<Comment> sortComments(String criteria) throws SQLDataException {

        List<Comment> comments = new ArrayList<>();

        String query = "SELECT * FROM comment";

        if (criteria.equalsIgnoreCase("date")) {
            query += " ORDER BY created_at DESC";
        } else if (criteria.equalsIgnoreCase("content")) {
            query += " ORDER BY content ASC";
        } else {
            query += " ORDER BY id DESC";
        }

        try {

            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {

                Comment c = new Comment();

                c.setId(rs.getInt("id"));
                c.setContent(rs.getString("content"));
                c.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                c.setPost_id(rs.getInt("post_id"));
                c.setUser_id(rs.getInt("user_id"));

                comments.add(c);
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return comments;
    }
}