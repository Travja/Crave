package me.travja.crave.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.travja.crave.common.models.auth.CraveUser;
import me.travja.crave.common.models.item.ItemDetails;
import me.travja.crave.common.models.item.PendingDetails;
import me.travja.crave.common.models.sale.Sale;
import me.travja.crave.common.models.store.Store;
import me.travja.crave.common.rabbit.RabbitMqService;
import me.travja.crave.common.repositories.UserRepo;
import me.travja.crave.common.util.Formatter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class AsyncCaller {

    private final  UserRepo        userRepo;
    private final  RabbitMqService rabbitMqService;
    private static DecimalFormat   decimalFormat = new DecimalFormat("#.##");

    @Async
    public void handlePriceUpdates(Map<ItemDetails, Double> priceChanges) {
        log.info("Running price handling!");

        String store = null;

        for (CraveUser user : userRepo.findAll()) {
            StringBuilder messageBody = new StringBuilder();
            int           count       = 0;
            if (user.getFavorites().isEmpty()) continue;

            for (ItemDetails details : priceChanges.keySet()) {
                if (user.getFavorites().stream()
                        .filter(itm -> itm.getStringUpc().equals(details.getItem().getStringUpc())).count() == 0) {
                    continue;
                }

                if (store == null) store = details.getStore().getName();

                count++;
                double change = priceChanges.get(details);

                messageBody.append("\t")
                        .append(details.getItem().getName())
                        .append(": ")
                        .append(Formatter.formatCurrency(details.getPrice()))
                        .append(change > 0 ? " (+" : " (")
                        .append(Formatter.formatCurrency(change))
                        .append(")\n");
            }

            if (count > 0)
                sendMessage(user, store, messageBody.toString());
        }
    }

    @Async
    public void handleSaleApproved(List<Sale> sales) {
        log.info("Running sale handling!");

        if (sales.isEmpty()) {
            log.info("Sales list is empty.");
            return;
        }

        Store store = sales.get(0).getStore();

        for (CraveUser user : userRepo.findAll()) {
            if (user.getFavorites().isEmpty()) continue;

            List<String> favoriteUpcs = user.getFavorites().stream()
                    .map(item -> item.getStringUpc()).collect(Collectors.toList());
            List<Sale> filtered = sales.stream()
                    .filter(sale -> favoriteUpcs.contains(sale.getItem().getItem().getStringUpc()))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) continue;

            StringBuilder messageBody = new StringBuilder();
            messageBody.append("There is a sale happening at ")
                    .append(store.getName()).append(" located at ")
                    .append(store.getStreetAddress()).append(", ").append(store.getCity())
                    .append("\n\nThis sale includes the following items which you have favorited:\n\n");

            if (user.getFavorites().isEmpty()) continue;

            for (Sale sale : filtered) {
                ItemDetails details = sale.getItem();

                messageBody.append("\t")
                        .append(details.getItem().getName())
                        .append(": ")
                        .append(Formatter.formatCurrency(details.getPrice()))
                        .append("\u2192")
                        .append(Formatter.formatCurrency(sale.getNewPrice()))
                        .append("\n");
            }

            messageBody.append("\nHappy shopping!");
            queueEmail(user.getEmail(), "Items are on sale!", messageBody.toString());
        }
    }

    @Async
    public void handleApprovalNeeded(List<PendingDetails> pendingItems) {
        log.info("Running pending handling!");
        if (pendingItems.isEmpty()) return;

        StringBuilder messageBody = new StringBuilder();

        for (PendingDetails pending : pendingItems) {
            messageBody.append(pending.getItem().getName())
                    .append(" at '").append(pending.getStore().getName())
                    .append(" - ")
                    .append(pending.getStore().getStreetAddress())
                    .append("' had a price change from ").append(Formatter.formatCurrency(pending.getOriginalPrice()))
                    .append(" to ").append(Formatter.formatCurrency(pending.getPrice()))
                    .append(pending.getPrice() < pending.getOriginalPrice() ? ", a decrease " : ", an increase ")
                    .append("of ").append(decimalFormat.format(pending.calculatePercentChange())).append("%")
                    .append("\n\n");
        }

        messageBody.append("You can approve these items at https://crave.travja.win/admin/approval\n\n")
                .append("Thank you!\n-The Crave Team");

        queueEmail(userRepo.findByUsernameIgnoreCase("services").get().getEmail(),
                "There are pending items that need your attention!",
                messageBody.toString());
    }

    public void sendMessage(CraveUser user, String store, String message) {
        StringBuilder body = new StringBuilder("We just wanted to inform you about a couple price changes of items " +
                "that you have favorited!\n\nItems:\n");
        body.append(message);
        body.append("\n\nThank you for using Crave!");

        queueEmail(user.getEmail(), "Prices update!", body.toString());
    }

    public void queueEmail(String to, String subject, String body) {
        Map<String, String> emailPayload = new HashMap<>();

        emailPayload.put("action", "email");
        emailPayload.put("email", to);
        emailPayload.put("subject", subject);
        emailPayload.put("body", body);

        rabbitMqService.send(emailPayload);
        log.info("Queued message to " + to);
    }

}