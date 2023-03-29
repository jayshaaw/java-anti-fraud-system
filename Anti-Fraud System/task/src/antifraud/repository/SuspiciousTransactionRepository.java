package antifraud.repository;

import antifraud.model.SuspiciousIP;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SuspiciousTransactionRepository extends CrudRepository<SuspiciousIP, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from SuspiciousIP s where upper(s.ip) = upper(?1)")
    void deleteByIpIgnoreCase(String ip);
    @Query("select (count(s) > 0) from SuspiciousIP s where upper(s.ip) = upper(?1)")
    boolean existsByIpIgnoreCase(String ip);
    @Query("select s from SuspiciousIP s where upper(s.ip) = upper(?1)")
    SuspiciousIP findByIpIgnoreCase(String ip);
}
