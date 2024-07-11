package engine.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletedQuizRepository extends PagingAndSortingRepository<CompletedQuiz, Long>, CrudRepository<CompletedQuiz, Long> {
    @Query("SELECT cq FROM CompletedQuiz cq WHERE cq.user.id = :userId")
    Page<CompletedQuiz> findAllCompletedQuizzesWithPagination(@Param("userId") Long userId, Pageable pageable);
}
