package antifraud.repository;

import antifraud.model.Users;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends CrudRepository<Users, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Users u set u.operation = ?1 where upper(u.username) = upper(?2)")
    void updateOperationByUsernameIgnoreCase(String operation, String username);
    @Query("select count(u) from Users u")
    long countFirstBy();
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Users u set u.role = ?1 where upper(u.username) = upper(?2)")
    void updateRoleByUsernameIgnoreCase(String role, String username);

    @Query("select u from Users u where upper(u.username) = upper(?1)")
    Users findByUsernameIgnoreCase(String username);

    @Query("select (count(u) > 0) from Users u where upper(u.username) = upper(?1)")
    boolean existsByUsernameIgnoreCase(String username);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete from Users u where upper(u.username) = upper(?1)")
    void deleteByUsernameIgnoreCase(String username);


}
