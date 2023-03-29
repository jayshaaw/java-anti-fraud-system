package antifraud.repository;

import antifraud.model.StolenCard;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StolenCardRepository extends CrudRepository<StolenCard, Long> {
    @Query("select s from StolenCard s where upper(s.number) = upper(?1)")
    StolenCard findByNumberIgnoreCase(String number);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from StolenCard s where upper(s.number) = upper(?1)")
    void deleteByNumberIgnoreCase(String number);
    @Query("select (count(s) > 0) from StolenCard s where upper(s.number) = upper(?1)")
    boolean existsByNumberIgnoreCase(String number);
}
