package pl.gov.coi.pomocua.ads.translations;

import lombok.EqualsAndHashCode;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pl.gov.coi.pomocua.ads.BaseOffer;
import pl.gov.coi.pomocua.ads.Location;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

import static javax.persistence.EnumType.STRING;

@EqualsAndHashCode(callSuper = true)
@Entity
public class TranslationOffer extends BaseOffer {

    @Enumerated(STRING)
    @NotNull
    public Mode mode;

    @ElementCollection(targetClass = Language.class)
    @CollectionTable
    @Enumerated(STRING)
    @NotEmpty
    public List<Language> language;

    @Embedded
    @Valid
    public Location location;

    public boolean sworn;

    enum Mode {
        REMOTE
    }

    enum Language {
        UA, PL
    }
}

@Repository
interface TranslationOfferRepository extends PagingAndSortingRepository<TranslationOffer, Long> {
}