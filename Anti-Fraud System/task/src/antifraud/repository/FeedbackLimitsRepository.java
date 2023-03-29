package antifraud.repository;

import antifraud.model.FeedbackLimits;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FeedbackLimitsRepository extends CrudRepository<FeedbackLimits, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update FeedbackLimits f set f.allowed = ?1, f.manual = ?2, f.maxAllowedAmount = ?3, f.maxManualAmount = ?4
            where upper(f.number) = upper(?5)""")
    void updateAllowedAndManualAndMaxAllowedAmountAndMaxManualAmountByNumberIgnoreCase(Long allowed, Long manual, Long maxAllowedAmount, Long maxManualAmount, String number);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update FeedbackLimits f set f.manual = ?1, f.maxManualAmount = ?2 where upper(f.number) = upper(?3)")
    void updateManualAndMaxManualAmountByNumberIgnoreCase(Long manual, Long maxManualAmount, String number);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update FeedbackLimits f set f.allowed = ?1, f.maxAllowedAmount = ?2 where upper(f.number) = upper(?3)")
    void updateAllowedAndMaxAllowedAmountByNumberIgnoreCase(Long allowed, Long maxAllowedAmount, String number);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update FeedbackLimits f set f.manual = ?1 where upper(f.number) = upper(?2)")
    void updateManualByNumberIgnoreCase(Long manual, String number);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update FeedbackLimits f set f.allowed = ?1 where upper(f.number) = upper(?2)")
    void updateAllowedByNumberIgnoreCase(Long allowed, String number);

    @Query("select f from FeedbackLimits f where upper(f.number) = upper(?1)")
    FeedbackLimits findByNumberIgnoreCase(String number);

    @Query("select (count(f) > 0) from FeedbackLimits f where upper(f.number) = upper(?1)")
    boolean existsByNumberIgnoreCase(String number);

}
