package pl.gov.coi.pomocua.ads.transport;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.EqualsAndHashCode;
import pl.gov.coi.pomocua.ads.BaseOffer;
import pl.gov.coi.pomocua.ads.Location;
import pl.gov.coi.pomocua.ads.UserId;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@EqualsAndHashCode(callSuper = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class TransportOffer extends BaseOffer {

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "region", column = @Column(name = "origin_region")),
            @AttributeOverride(name = "city", column = @Column(name = "origin_city"))
    })
    @Valid
    public Location origin;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "region", column = @Column(name = "destination_region")),
            @AttributeOverride(name = "city", column = @Column(name = "destination_city"))
    })
    @Valid
    public Location destination;

    @NotNull
    @Min(1)
    @Max(99)
    public Integer capacity;

    @NotNull
    public LocalDate transportDate;

    public static TransportOffer of(String title, String description, UserId user, Location origin, Location destination, Integer capacity) {
        TransportOffer transportOffer = new TransportOffer();
        transportOffer.title = title;
        transportOffer.description = description;
        transportOffer.userId = user;
        transportOffer.origin = origin;
        transportOffer.destination = destination;
        transportOffer.capacity = capacity;
        transportOffer.transportDate = LocalDate.now();
        return transportOffer;
    }
}
