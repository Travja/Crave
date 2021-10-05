package me.travja.crave.receiptservice;

import me.travja.crave.receiptservice.models.Receipt;
import org.springframework.data.repository.CrudRepository;

public interface ReceiptRepository extends CrudRepository<Receipt, Long> {

}
