package com.cardiolink.Services;

import com.cardiolink.Models.Post;
import com.cardiolink.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePost implements Iservice<Post> {

    private Connection cnx;

    public ServicePost() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Post post) throws SQLDataException {
        try {

            String query = "INSERT INTO post (title, content, created_at, user_id) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(post.getCreated_at()));
            ps.setInt(4, post.getUser_id());

            ps.executeUpdate();

            System.out.println("Post ajouté !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void update(Post post) throws SQLDataException {
        try {

            String query = "UPDATE post SET title=?, content=?, created_at=?, user_id=? WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(post.getCreated_at()));
            ps.setInt(4, post.getUser_id());
            ps.setInt(5, post.getId());

            ps.executeUpdate();

            System.out.println("Post modifié !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public void delete(Post post) throws SQLDataException {
        try {

            String query = "DELETE FROM post WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);

            ps.setInt(1, post.getId());

            ps.executeUpdate();

            System.out.println("Post supprimé !");

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

    @Override
    public List<Post> getAll() throws SQLDataException {

        List<Post> posts = new ArrayList<>();

        try {

            String query = "SELECT * FROM post";

            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {

                Post p = new Post();

                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setContent(rs.getString("content"));
                p.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                p.setUser_id(rs.getInt("user_id"));

                posts.add(p);
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return posts;
    }

    @Override
    public Post getById(int id) throws SQLDataException {

        Post p = null;

        try {

            String query = "SELECT * FROM post WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                p = new Post();

                p.setId(rs.getInt("id"));
                p.setTitle(rs.getString("title"));
                p.setContent(rs.getString("content"));
                p.setCreated_at(rs.getTimestamp("created_at").toLocalDateTime());
                p.setUser_id(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return p;
    }
}