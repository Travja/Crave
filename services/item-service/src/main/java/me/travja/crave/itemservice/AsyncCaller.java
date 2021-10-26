package me.travja.crave.itemservice;

import lombok.AllArgsConstructor;
import me.travja.crave.common.models.CraveUser;
import me.travja.crave.common.models.ItemDetails;
import me.travja.crave.common.rabbit.RabbitMqService;
import me.travja.crave.common.repositories.UserRepo;
import me.travja.crave.common.util.Formatter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class AsyncCaller {

    private final UserRepo        userRepo;
    private final RabbitMqService rabbitMqService;

    @Async
    public void handlePriceUpdates(Map<ItemDetails, Double> priceChanges) {
        System.out.println("Running price handling!");

        StringBuilder messageBody = new StringBuilder();
        String        store       = null;

        for (CraveUser user : userRepo.findAll()) {
            int count = 0;
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

    public void sendMessage(CraveUser user, String store, String message) {

        Map<String, String> emailPayload = new HashMap<>();

        StringBuilder body = new StringBuilder("We just wanted to inform you about a couple price changes of items " +
                "that you have favorited!\n\nItems:\n");
        body.append(message);
        body.append("\n\nThank you for using Crave!");

        emailPayload.put("action", "email");
        emailPayload.put("email", user.getEmail());
        emailPayload.put("subject", "Prices update!");
        emailPayload.put("body", body.toString());

        rabbitMqService.send(emailPayload);
        System.out.println("Queued message to " + user.getEmail());
    }

}