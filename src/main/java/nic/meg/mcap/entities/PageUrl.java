package nic.meg.mcap.entities;

import jakarta.persistence.*;
import lombok.*;
import nic.meg.mcap.audit.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageUrl {

    @Id
    private short urlCode;

    @ManyToOne
    @JoinColumn(name = "menuId")
    private Menu menu;

    private String pageUrl;

    private String method;

    private Boolean isPublic = false;
}