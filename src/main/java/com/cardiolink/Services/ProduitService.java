package com.cardiolink.Services;

import com.cardiolink.Models.Produit;
import com.cardiolink.utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements Iservice<Produit> {

    private final Connection cnx = MyDatabase.getInstance().getConnection();

    private Produit mapResultSet(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getInt("id"));

        setFieldDirectly(p, "nom", rs.getString("nom"));
        p.setDescription(rs.getString("description"));

        BigDecimal prix = rs.getBigDecimal("prix");
        if (prix != null && prix.compareTo(BigDecimal.ZERO) > 0) {
            try {
                p.setPrix(prix);
            } catch (Exception ignored) {
                setFieldDirectly(p, "prix", prix);
            }
        } else {
            p.setPrix(BigDecimal.ONE);
        }

        int stock = rs.getInt("stock");
        try {
            p.setStock(stock);
        } catch (Exception ignored) {
            p.setStock(0);
        }

        String imgUrl = rs.getString("image_url");
        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            try {
                p.setImageUrl(imgUrl);
            } catch (Exception ignored) {
                setFieldDirectly(p, "imageUrl", imgUrl);
            }
        }

        String cat = rs.getString("categorie");
        if (cat != null && !cat.trim().isEmpty()) {
            try {
                p.setCategorie(cat);
            } catch (Exception ignored) {
                setFieldDirectly(p, "categorie", cat.toUpperCase());
            }
        } else {
            try {
                p.setCategorie("AUTRE");
            } catch (Exception ignored) {
            }
        }

        return p;
    }

    private void setFieldDirectly(Produit p, String fieldName, Object value) {
        try {
            java.lang.reflect.Field f = Produit.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(p, value);
        } catch (Exception ignored) {
        }
    }

    // ─── Validation métier
    private void valider(Produit p) {
        if (p == null)
            throw new IllegalArgumentException("Le produit ne peut pas être null.");

        // ── Nom ──
        if (p.getNom() == null || p.getNom().trim().isEmpty())
            throw new IllegalArgumentException("Le nom est obligatoire.");
        String nom = p.getNom().trim();
        if (nom.length() < 2)
            throw new IllegalArgumentException("Le nom doit contenir au moins 2 caractères.");
        if (nom.length() > 100)
            throw new IllegalArgumentException("Le nom ne doit pas dépasser 100 caractères.");
        if (!Character.isLetter(nom.charAt(0)))
            throw new IllegalArgumentException("Le nom doit commencer par une lettre.");
        if (!nom.matches("^[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ0-9\\s''()\\-+°%/]{1,99}$"))
            throw new IllegalArgumentException("Le nom doit commencer par une lettre et ne contenir que des caractères valides.");

        // ── Prix ──
        if (p.getPrix() == null || p.getPrix().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Le prix doit être > 0.");
        if (p.getPrix().compareTo(new BigDecimal("99999.99")) > 0)
            throw new IllegalArgumentException("Le prix ne peut pas dépasser 99 999.99 DT.");

        // ── Stock ──
        if (p.getStock() == null || p.getStock() < 0)
            throw new IllegalArgumentException("Le stock doit être >= 0.");
        if (p.getStock() > 999999)
            throw new IllegalArgumentException("Le stock ne peut pas dépasser 999 999.");

        // ── Catégorie ──
        if (p.getCategorie() == null || p.getCategorie().trim().isEmpty())
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        if (p.getCategorie().trim().length() > 50)
            throw new IllegalArgumentException("La catégorie ne doit pas dépasser 50 caractères.");
    }

    // ─── Unicité du nom
    public boolean existsByNom(String nom) {
        String sql = "SELECT COUNT(*) FROM produit WHERE LOWER(TRIM(nom)) = LOWER(TRIM(?))";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ existsByNom : " + e.getMessage());
        }
        return false;
    }

    public boolean existsByNomExcludingId(String nom, int excludeId) {
        String sql = "SELECT COUNT(*) FROM produit WHERE LOWER(TRIM(nom)) = LOWER(TRIM(?)) AND id != ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ existsByNomExcludingId : " + e.getMessage());
        }
        return false;
    }

    // ─── CRUD selon Iservice
    @Override
    public void add(Produit produit) {
        valider(produit);
        if (existsByNom(produit.getNom()))
            throw new IllegalArgumentException("Un produit avec ce nom existe déjà.");

        String sql = "INSERT INTO produit (nom, description, prix, stock, image_url, categorie) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setBigDecimal(3, produit.getPrix());
            ps.setInt(4, produit.getStock());

            if (produit.getImageUrl() == null) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, produit.getImageUrl());

            if (produit.getCategorie() == null) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, produit.getCategorie());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) produit.setId(keys.getInt(1));
            }

            System.out.println("✅ Produit ajouté : " + produit.getNom() + " ID=" + produit.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Produit produit) {
        valider(produit);

        if (produit.getId() == null)
            throw new IllegalArgumentException("L'id est requis pour la mise à jour.");

        if (existsByNomExcludingId(produit.getNom(), produit.getId()))
            throw new IllegalArgumentException("Un autre produit avec ce nom existe déjà.");

        String sql = "UPDATE produit SET nom=?, description=?, prix=?, stock=?, image_url=?, categorie=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, produit.getNom());
            ps.setString(2, produit.getDescription());
            ps.setBigDecimal(3, produit.getPrix());
            ps.setInt(4, produit.getStock());

            if (produit.getImageUrl() == null) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, produit.getImageUrl());

            if (produit.getCategorie() == null) ps.setNull(6, Types.VARCHAR);
            else ps.setString(6, produit.getCategorie());

            ps.setInt(7, produit.getId());

            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new RuntimeException("Produit introuvable (id=" + produit.getId() + ").");

            System.out.println("✅ Produit modifié : " + produit.getNom());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Produit produit) {
        if (produit == null || produit.getId() == null)
            throw new IllegalArgumentException("Produit invalide.");

        if (isUsedInCommande(produit.getId()))
            throw new IllegalStateException("Suppression interdite : produit utilisé dans une commande.");

        String sql = "DELETE FROM produit WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, produit.getId());

            int rows = ps.executeUpdate();
            if (rows == 0)
                throw new RuntimeException("Produit introuvable (id=" + produit.getId() + ").");

            System.out.println("✅ Produit supprimé ID=" + produit.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Produit> getAll() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT id, nom, description, prix, stock, image_url, categorie FROM produit ORDER BY id DESC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ getAll : " + e.getMessage());
        }

        return list;
    }

    @Override
    public Produit getById(int id) {
        String sql = "SELECT id, nom, description, prix, stock, image_url, categorie FROM produit WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ getById : " + e.getMessage());
        }

        return null;
    }

    public void add2(Produit produit) {
        add(produit);
    }

    public List<Produit> getall() {
        return getAll();
    }


    private boolean isUsedInCommande(int produitId) {
        String sql = "SELECT COUNT(*) FROM ligne_commande WHERE produit_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, produitId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ isUsedInCommande : " + e.getMessage());
        }
        return false;
    }

    public List<Produit> search(String query) {
        return searchWithFilter(query, null);
    }

    public List<Produit> getByCategorie(String categorie) {
        return searchWithFilter(null, categorie);
    }

    public List<Produit> searchWithFilter(String query, String categorie) {
        List<Produit> list = new ArrayList<>();
        boolean hasQ = query != null && !query.trim().isEmpty();
        boolean hasCat = categorie != null && !categorie.trim().isEmpty();

        StringBuilder sql = new StringBuilder(
                "SELECT id, nom, description, prix, stock, image_url, categorie FROM produit WHERE 1=1"
        );

        if (hasQ) sql.append(" AND (nom LIKE ? OR description LIKE ?)");
        if (hasCat) sql.append(" AND UPPER(TRIM(categorie)) = UPPER(TRIM(?))");
        sql.append(" ORDER BY id DESC");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            int idx = 1;

            if (hasQ) {
                String pat = "%" + query.trim() + "%";
                ps.setString(idx++, pat);
                ps.setString(idx++, pat);
            }

            if (hasCat) ps.setString(idx, categorie.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ searchWithFilter : " + e.getMessage());
        }

        return list;
    }

    public List<String> findExistingCategories() {
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT categorie FROM produit " +
                "WHERE categorie IS NOT NULL AND TRIM(categorie) != '' ORDER BY categorie ASC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cats.add(rs.getString("categorie"));
            }
        } catch (SQLException e) {
            System.err.println("❌ findExistingCategories : " + e.getMessage());
        }

        return cats;
    }

    public void updateStock(int produitId, int newStock) {
        if (newStock < 0)
            throw new IllegalArgumentException("Le stock ne peut pas être négatif.");

        String sql = "UPDATE produit SET stock=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setInt(2, produitId);

            if (ps.executeUpdate() == 0)
                throw new RuntimeException("Produit introuvable (id=" + produitId + ").");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur stock : " + e.getMessage(), e);
        }
    }

    public List<Produit> getProduitsEnRupture() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT id, nom, description, prix, stock, image_url, categorie FROM produit WHERE stock = 0 ORDER BY nom ASC";

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapResultSet(rs));
        } catch (SQLException e) {
            System.err.println("❌ getProduitsEnRupture : " + e.getMessage());
        }

        return list;
    }
}