package engine.quiz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import engine.user.User;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Entity
@Table
public class Quiz {
    @Id
    @Column
    @GeneratedValue
    private long id;

    @Column
    @NotBlank
    private String title;

    @Column
    @NotBlank
    private String text;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @NotNull
    @Size(min = 2)
    private List<String> options;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Integer> answer;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private List<CompletedQuiz> quizCompleteds = new ArrayList<>();

    public boolean isCorrect(List<Integer> incomingAnswer) {
        if (answer == null ) {
            return incomingAnswer == null || incomingAnswer.isEmpty();
        } else {
            return answer.size() == incomingAnswer.size() && new HashSet<>(answer).containsAll(incomingAnswer);
        }
    }

    public void setQuizCompleteds(List<CompletedQuiz> quizCompleteds) {
        this.quizCompleteds = quizCompleteds;
    }

    public List<CompletedQuiz> getQuizCompleteds() {
        return quizCompleteds;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setAnswer(List<Integer> answer) {
        this.answer = answer;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public User getUser() {
        return user;
    }

    public List<Integer> getAnswer() {
        return answer;
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Quiz{");
        sb.append("id=").append(id);
        sb.append(", title='").append(title).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", options=").append(options);
        sb.append(", answer=").append(answer);
        sb.append('}');
        return sb.toString();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
