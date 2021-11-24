package me.travja.crave.itemservice;

import com.fasterxml.jackson.annotation.JsonView;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import me.travja.crave.common.annotations.CraveController;
import me.travja.crave.common.models.item.PendingDetails;
import me.travja.crave.common.repositories.ItemService;
import me.travja.crave.common.views.CraveViews;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequiredArgsConstructor
@CraveController("/pending")
public class PendingRestController {

    private final ItemService itemService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(CraveViews.DetailsView.class)
    public List<PendingDetails> getPending() {
        return itemService.getAllPending();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(CraveViews.DetailsView.class)
    public PendingDetails getById(@PathVariable long id) {
        return itemService.getPending(id);
    }


    @Transactional
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable long id) throws NotFoundException {
        PendingDetails pending = itemService.getPending(id);
        if (pending == null) throw new NotFoundException("Item by that ID was not found.");

        pending.approve();
    }

}
