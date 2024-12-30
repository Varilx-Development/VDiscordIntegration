package de.varilx.discordIntegration.entity;

import de.varilx.database.Id;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LinkCode {

    @Id
    @jakarta.persistence.Id
    UUID link;

    Long code;

    String username;


    Long timestamp;

}
