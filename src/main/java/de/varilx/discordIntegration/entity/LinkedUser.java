package de.varilx.discordIntegration.entity;

import de.varilx.database.id.MongoId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LinkedUser {

    @MongoId
    @Id
    UUID _id;

    Long discordId;

    UUID uuid;

    String ingameName;

}
