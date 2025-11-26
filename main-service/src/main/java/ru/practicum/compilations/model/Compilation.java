package ru.practicum.compilations.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.events.model.Event;

import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    private Boolean pinned;

    private String title;
}
