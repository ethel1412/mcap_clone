package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.*;
import nic.meg.mcap.audit.AuditingEntityListener;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private short menuId;

    private String menuName;

    private String iconClass;  // Add this field

    private Integer orderIndex = 0;

    private Boolean isActive = true;

    @ManyToMany(mappedBy = "menus")
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "parent_menu_id")
    private Menu parentMenu;

    @OneToMany(mappedBy = "parentMenu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Menu> children;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PageUrl> pageUrls;
}