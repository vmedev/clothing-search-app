package lv.rtu.clothingsearchapp.model;

import jakarta.persistence.*;

// Veikals, kurā pārdod preci (tabula Store)
@Entity
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Veikala mājaslapa
    @Column(name = "website_url")
    private String websiteUrl;

    public Store() {}

    public Store(String name, String websiteUrl) {
        this.name = name;
        this.websiteUrl = websiteUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
}
