package com.iss.eventorium.solution.models;

import com.iss.eventorium.category.models.Category;
import com.iss.eventorium.event.models.EventType;
import com.iss.eventorium.interaction.models.Rating;
import com.iss.eventorium.shared.models.ImagePath;
import com.iss.eventorium.shared.models.Status;
import com.iss.eventorium.shared.utils.ImageHolder;
import com.iss.eventorium.user.models.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;
import java.util.Objects;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@SQLRestriction("is_deleted = false")
public abstract class Solution implements ImageHolder {

    @Id
    @SequenceGenerator(name = "solutionSeqGen", sequenceName = "solutionSequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "solutionSeqGen")
    private Long id;

    @Column(nullable = false)
    @Size(max = 75)
    private String name;

    @Column(nullable = false, length = 1000)
    @Size(max = 750)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Double discount;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="is_available")
    private Boolean isAvailable;

    @Column(name="is_deleted")
    private Boolean isDeleted;

    @Column(name="is_visible")
    private Boolean isVisible;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "solution_id")
    private List<Rating> ratings;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="solution_event_types", joinColumns = @JoinColumn(name = "solution_id"), inverseJoinColumns = @JoinColumn(name = "event_type_id"))
    private List<EventType> eventTypes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ImagePath> imagePaths;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "provider_id")
    private User provider;

    public abstract void restore(Memento memento);

    public Double calculateAverageRating() {
        if(getRatings() != null) {
            return getRatings()
                    .stream()
                    .mapToInt(Rating::getRating)
                    .average()
                    .orElse(0.0d);
        } else {
            return 0.0d;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution solution)) return false;
        return Objects.equals(id, solution.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}