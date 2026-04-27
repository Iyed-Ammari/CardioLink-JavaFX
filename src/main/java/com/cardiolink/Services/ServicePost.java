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
    public void add(Post p) throws SQLDataException {
        try {
            String sql = "INSERT INTO post (title, content, image, user_id, created_at) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getContent());
            ps.setString(3, p.getImage());
            ps.setInt(4, p.getUser_id());
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();

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
            // 1. On supprime d'abord tous les commentaires liés à ce post
            // pour éviter l'erreur de contrainte (Foreign Key constraint)
            String queryComments = "DELETE FROM comment WHERE post_id=?";
            PreparedStatement psComm = cnx.prepareStatement(queryComments);
            psComm.setInt(1, post.getId());
            psComm.executeUpdate();

            // 2. Maintenant on peut supprimer le post
            String queryPost = "DELETE FROM post WHERE id=?";
            PreparedStatement psPost = cnx.prepareStatement(queryPost);
            psPost.setInt(1, post.getId());

            int rowsAffected = psPost.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Post et ses commentaires associés supprimés !");
            }

        } catch (SQLException e) {
            throw new SQLDataException("Erreur lors de la suppression : " + e.getMessage());
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

                // 🔥 IMPORTANT FIX
                p.setImage(rs.getString("image"));

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
    public List<Post> sortPosts(String criteria) throws SQLDataException {

        List<Post> posts = new ArrayList<>();

        String query = "SELECT * FROM post";

        if (criteria.equalsIgnoreCase("date")) {
            query += " ORDER BY created_at DESC";
        }
        else if (criteria.equalsIgnoreCase("title")) {
            query += " ORDER BY title ASC";
        }
        else {
            query += " ORDER BY id DESC";
        }

        try {

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
    public List<Post> searchByTitle(String keyword) throws SQLDataException {

        List<Post> posts = new ArrayList<>();

        try {

            String query = "SELECT * FROM post WHERE title LIKE ? ORDER BY title ASC";

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, keyword + "%");

            ResultSet rs = ps.executeQuery();

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
    public int countPostsByUser(int userId) throws SQLDataException {

        int count = 0;

        try {

            String query = "SELECT COUNT(*) FROM post WHERE user_id=?";

            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }

        return count;
    }
    public List<Post> getRecentPosts() throws SQLDataException {

        List<Post> posts = new ArrayList<>();

        try {

            String query = "SELECT * FROM post WHERE created_at >= NOW() - INTERVAL 7 DAY";

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
    public List<Post> filterByPeriod(String period) throws SQLDataException {

        List<Post> posts = new ArrayList<>();

        String query = "SELECT * FROM post WHERE created_at >= ";

        if (period.equalsIgnoreCase("24h")) {
            query += "NOW() - INTERVAL 1 DAY";
        }
        else if (period.equalsIgnoreCase("7d")) {
            query += "NOW() - INTERVAL 7 DAY";
        }
        else if (period.equalsIgnoreCase("30d")) {
            query += "NOW() - INTERVAL 30 DAY";
        }
        else {
            query += "NOW() - INTERVAL 365 DAY"; // fallback
        }

        try {

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
    //likes
    public boolean toggleLike(int postId, int userId) throws SQLDataException {
        try {
            // 1. Vérifier si déjà liké
            String check = "SELECT 1 FROM post_likes WHERE post_id=? AND user_id=?";
            PreparedStatement psCheck = cnx.prepareStatement(check);
            psCheck.setInt(1, postId);
            psCheck.setInt(2, userId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // Déjà liké -> On retire le Like (UNLIKE)
                String delete = "DELETE FROM post_likes WHERE post_id=? AND user_id=?";
                PreparedStatement psDel = cnx.prepareStatement(delete);
                psDel.setInt(1, postId);
                psDel.setInt(2, userId);
                psDel.executeUpdate();
                return false;
            } else {
                // Pas encore liké -> On va LIKER

                // ÉTAPE CRUCIALE : On supprime le dislike s'il existe
                String clearDislike = "DELETE FROM post_dislike WHERE post_id=? AND user_id=?";
                PreparedStatement psClear = cnx.prepareStatement(clearDislike);
                psClear.setInt(1, postId);
                psClear.setInt(2, userId);
                psClear.executeUpdate();

                // Puis on ajoute le Like
                String insert = "INSERT INTO post_likes (post_id, user_id) VALUES (?, ?)";
                PreparedStatement psIns = cnx.prepareStatement(insert);
                psIns.setInt(1, postId);
                psIns.setInt(2, userId);
                psIns.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }
    public int countLikes(int postId) throws SQLDataException {
        try {
            String query = "SELECT COUNT(*) FROM post_likes WHERE post_id=?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, postId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLDataException(e.getMessage());
        }
    }
    public boolean isLikedByUser(int postId, int userId) throws SQLDataException {
        try {
            String query = "SELECT 1 FROM post_likes WHERE post_id=? AND user_id=?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, postId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLDataException(e.getMessage());
        }
    }
    public int getUserFlames(int userId) throws SQLDataException {
        try {

            String query = "SELECT created_at FROM post WHERE user_id=? ORDER BY created_at DESC";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            int flames = 0;
            Timestamp previous = null;

            long now = System.currentTimeMillis();

            while (rs.next()) {

                Timestamp postDate = rs.getTimestamp("created_at");

                if (previous == null) {
                    long diff = now - postDate.getTime();

                    // TEST 1 min
                    if (diff <= 60000) { // 1h
                        flames = 1;
                        previous = postDate;
                    } else {
                        break;
                    }

                } else {

                    long diff = previous.getTime() - postDate.getTime();

                    if (diff <= 60000) {
                        flames++;
                        previous = postDate;
                    } else {
                        break;
                    }
                }
            }

            return flames;

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }
    public boolean toggleDislike(int postId, int userId) throws SQLDataException {
        try {
            // 1. Vérifier si déjà disliké
            String check = "SELECT 1 FROM post_dislike WHERE post_id=? AND user_id=?";
            PreparedStatement psCheck = cnx.prepareStatement(check);
            psCheck.setInt(1, postId);
            psCheck.setInt(2, userId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // Déjà disliké -> On retire le Dislike
                String delete = "DELETE FROM post_dislike WHERE post_id=? AND user_id=?";
                PreparedStatement psDel = cnx.prepareStatement(delete);
                psDel.setInt(1, postId);
                psDel.setInt(2, userId);
                psDel.executeUpdate();
                return false;
            } else {
                // Pas encore disliké -> On va DISLIKER

                // ÉTAPE CRUCIALE : On supprime le like s'il existe
                String clearLike = "DELETE FROM post_likes WHERE post_id=? AND user_id=?";
                PreparedStatement psClear = cnx.prepareStatement(clearLike);
                psClear.setInt(1, postId);
                psClear.setInt(2, userId);
                psClear.executeUpdate();

                // Puis on ajoute le Dislike
                String insert = "INSERT INTO post_dislike (post_id, user_id) VALUES (?, ?)";
                PreparedStatement psIns = cnx.prepareStatement(insert);
                psIns.setInt(1, postId);
                psIns.setInt(2, userId);
                psIns.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }
    public int countDislikes(int postId) throws SQLDataException {
        try {
            String query = "SELECT COUNT(*) FROM post_dislike WHERE post_id=?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, postId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }
    public boolean isDislikedByUser(int postId, int userId) throws SQLDataException {
        try {
            String query = "SELECT 1 FROM post_dislike WHERE post_id=? AND user_id=?";
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, postId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new SQLDataException(e.getMessage());
        }
    }

}
