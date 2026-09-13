package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.model.Category;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(nullable = false)
    String annotation;

    @Column(nullable = false)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventState state;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(nullable = false)
    Boolean paid;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User initiator;

    @Column(name = "participant_limit", nullable = false)
    Integer participantLimit;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(nullable = false)
    Double lat;

    @Column(nullable = false)
    Double lon;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests;

    @Column(name = "published_on")
    LocalDateTime publishedOn;
}
