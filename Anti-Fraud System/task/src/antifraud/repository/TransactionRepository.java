package antifraud.repository;

import antifraud.model.Transaction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Transaction t set t.feedback = ?1 where t.id = ?2")
    void updateFeedbackById(String feedback, Long id);


    List<Transaction> findByNumber(String number);
    
    @Query("select t from Transaction t where t.id = ?1")
    @Override
    Optional<Transaction> findById(Long aLong);

}
