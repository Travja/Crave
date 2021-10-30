package me.travja.crave.common.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import me.travja.crave.common.models.item.Item;
import me.travja.crave.common.models.item.ListItem;
import org.hibernate.Hibernate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CraveUser implements UserDetails {

    @Id
    private String username;
    private String email,
            password;

    @ManyToMany(cascade = CascadeType.ALL)
    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Item> favorites = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ListItem> shoppingList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> authList = new ArrayList<>();

    private boolean notificationsEnabled = true;

    public CraveUser(String username, String email, String password, List<String> authList) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.authList = authList;
    }

    public void setShoppingList(List<ListItem> shoppingList) {
        this.shoppingList.clear();
        this.shoppingList.addAll(shoppingList);
    }

    @JsonIgnore
    public List<? extends GrantedAuthority> getAuthorities() {
        return authList.stream()
                .map(auth -> new SimpleGrantedAuthority(auth)).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CraveUser craveUser = (CraveUser) o;
        return username != null && Objects.equals(username, craveUser.username);
    }
}
