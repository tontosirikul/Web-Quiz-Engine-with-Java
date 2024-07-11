package engine.quiz;


import engine.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@Validated
public class QuizController {

    private record QuizDTO(long id, String title, String text, List<String> options) {}
    private record CompletedQuizDTO(long id, LocalDateTime completedAt) {}
    private record QuizResponse(boolean success, String feedback) {}

    private final QuizRepository quizRepository;
    private final CompletedQuizRepository completedQuizRepository;

    public QuizController(QuizRepository quizRepository, CompletedQuizRepository completedQuizRepository) {
        this.quizRepository = quizRepository;
        this.completedQuizRepository = completedQuizRepository;
    }

    @GetMapping("/quizzes")
    public Page<QuizDTO> getQuizzes(@RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Quiz> quizzes = quizRepository.findAll(pageable);
        return quizzes.map(quiz ->
                new QuizDTO(quiz.getId(), quiz.getTitle(), quiz.getText(), quiz.getOptions()));
    }

    @GetMapping("/quizzes/{id}")
    public QuizDTO getQuiz(@PathVariable @Min(0) long id) {
        Optional<Quiz> result = quizRepository.findById(id);
        if (result.isPresent()) {
            Quiz quiz = result.get();
            return new QuizDTO(quiz.getId(), quiz.getTitle(), quiz.getText(), quiz.getOptions());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/quizzes")
    public Quiz createQuiz(@Valid @RequestBody Quiz quiz, @AuthenticationPrincipal User user) {
        quiz.setUser(user);
        quizRepository.save(quiz);
        return quiz;
    }

    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<QuizDTO> deleteQuiz(@PathVariable @Min(0) long id, @AuthenticationPrincipal User user) {
        Optional<Quiz> result = quizRepository.findById(id);
        if (result.isPresent()) {
            if (Objects.equals(result.get().getUser().getId(), user.getId())) {
                quizRepository.delete(result.get());
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/quizzes/{id}/solve")
    public ResponseEntity<QuizResponse> solveQuiz(@PathVariable @Min(0) long id, @RequestBody @NotNull Map<String, List<Integer>> body, @AuthenticationPrincipal User user) {
        Optional<Quiz> result = quizRepository.findById(id);
        if (result.isPresent()) {
            Quiz quiz = result.get();
            System.out.println(quiz);
            boolean isCorrect = quiz.isCorrect(body.get("answer"));
            String message = isCorrect ? "Congratulations, you're right!" : "Wrong answer! Please, try again.";
            QuizResponse quizResponse = new QuizResponse(isCorrect, message);

            if (isCorrect) {
                CompletedQuiz completedQuiz = new CompletedQuiz();
                completedQuiz.setQuiz(quiz);
                completedQuiz.setUser(user);
                completedQuiz.setCompletedAt(LocalDateTime.now());
                completedQuizRepository.save(completedQuiz);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(quizResponse);
        }
        else {
            System.out.println("here");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/quizzes/completed")
    public Page<CompletedQuizDTO> getQuizzes(@RequestParam int page, @AuthenticationPrincipal User user) {
        try {
            Pageable pageable = PageRequest.of(page, 10, Sort.by("completedAt").descending());
            Page<CompletedQuiz> completedQuizzes = completedQuizRepository.findAllCompletedQuizzesWithPagination(user.getId(), pageable);
            return completedQuizzes.map(completedQuiz ->
                    new CompletedQuizDTO(completedQuiz.getQuiz().getId(), completedQuiz.getCompletedAt()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving completed quizzes", e);
        }
    }

}
