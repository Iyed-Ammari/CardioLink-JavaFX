package com.cardiolink.Services;

import com.cardiolink.Models.Comment;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment implements Iservice<Comment> {

    private Connection cnx;

    public ServiceComment() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Comment comment) throws SQLDataException {
        try {

            String query = "INSERT INTO comment (content, created_at, post_id, user_id) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setString(1, comment.getContent());
            ps.setTimestamp(2, Timestamp.valueOf(comment.getCreated_at()));
            ps.setInt(3, comment.getPost_id());
            ps.setInt(4, comment.getUser_id());

            ps.executeUpdate();

            System.out.println("Comment ajouté !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void update(Comment comment) throws SQLDataException {
        try {

            String query = "UPDATE comment SET content=?, created_at=?, post_id=?, user_id=? WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setString(1, comment.getContent());
            ps.setTimestamp(2, Timestamp.valueOf(comment.getCreated_at()));
            ps.setInt(3, comment.getPost_id());
            ps.setInt(4, comment.getUser_id());
            ps.setInt(5, comment.getId());

            ps.executeUpdate();

            System.out.println("Comment modifié !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(Comment comment) throws SQLDataException {
        try {

            String query = "DELETE FROM comment WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setInt(1, comment.getId());

            ps.executeUpdate();

            System.out.println("Comment supprimé !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

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
}