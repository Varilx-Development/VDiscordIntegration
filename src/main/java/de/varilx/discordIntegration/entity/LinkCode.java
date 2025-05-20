package de.varilx.discordIntegration.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LinkCode {

    @Id
    UUID _id;

    UUID player;

    Long code;

    String username;

    Long timestamp;

}
