package me.travja.crave.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.travja.crave.common.models.Item;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CraveUser implements UserDetails {

    @Id
    private String username;
    private String email,
            password;

    @OneToMany
    private List<Item> favorites = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> authList;

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
}
