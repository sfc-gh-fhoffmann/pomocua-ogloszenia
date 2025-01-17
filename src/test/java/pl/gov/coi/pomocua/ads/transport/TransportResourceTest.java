package pl.gov.coi.pomocua.ads.transport;

import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
import pl.gov.coi.pomocua.ads.BaseResourceTest;
import pl.gov.coi.pomocua.ads.Location;
import pl.gov.coi.pomocua.ads.Offers;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TransportResourceTest extends BaseResourceTest<TransportOffer> {

    @Autowired
    private TransportOfferRepository repository;

    @Override
    protected Class<TransportOffer> getClazz() {
        return TransportOffer.class;
    }

    @Override
    protected String getOfferSuffix() {
        return "transport";
    }

    @BeforeEach
    public void clearDatabase() {
        repository.deleteAll();
    }

    @Override
    protected TransportOffer sampleOfferRequest() {
        TransportOffer transportOffer = new TransportOffer();
        transportOffer.title = "jade do Pcimia";
        transportOffer.description = "moge zabrac 20 osob";
        transportOffer.destination = new Location("Pomorskie", "Gdańsk");
        transportOffer.origin = new Location("Pomorskie", "Pruszcz Gdański");
        transportOffer.transportDate = LocalDate.of(2022, 4, 1);
        transportOffer.capacity = 28;
        return transportOffer;
    }

    @Override
    protected CrudRepository<TransportOffer, Long> getRepository() {
        return repository;
    }

    @Test
    void shouldFindByOrigin() {
        TransportOffer transportOffer1 = postOffer(aTransportOffer()
                .origin(new Location("mazowieckie", "warszawa"))
                .build());
        postOffer(aTransportOffer()
                .origin(new Location("Pomorskie", "Wejherowo"))
                .build());
        postOffer(aTransportOffer()
                .origin(new Location("Wielkopolskie", "Warszawa"))
                .build());

        TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
        searchCriteria.setOrigin(new Location("Mazowieckie", "Warszawa"));
        var results = searchOffers(searchCriteria);

        assertThat(results).hasSize(1);
        assertThat(results).first().isEqualTo(transportOffer1);
    }

    @Test
    void shouldFindByDestination() {
        TransportOffer transportOffer1 = postOffer(aTransportOffer()
                .destination(new Location("pomorskie", "GdyniA"))
                .build());
        postOffer(aTransportOffer()
                .destination(new Location("Pomorskie", "Wejherowo"))
                .build());

        TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
        searchCriteria.setDestination(new Location("Pomorskie", "Gdynia"));
        var results = searchOffers(searchCriteria);

        assertThat(results).hasSize(1);
        assertThat(results).first().isEqualTo(transportOffer1);
    }

    @Test
    void shouldFindByCapacity() {
        TransportOffer transportOffer1 = postOffer(aTransportOffer()
                .capacity(10)
                .build());
        TransportOffer transportOffer2 = postOffer(aTransportOffer()
                .capacity(11)
                .build());
        postOffer(aTransportOffer()
                .capacity(1)
                .build());

        TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
        searchCriteria.setCapacity(10);
        var results = searchOffers(searchCriteria);
        assertThat(results).containsExactly(transportOffer1,transportOffer2);
    }

    @Test
    void shouldFindByTransportDate() {
        TransportOffer transportOffer1 = postOffer(aTransportOffer()
                .transportDate(LocalDate.of(2022, 3, 21))
                .build());
        postOffer(aTransportOffer()
                .transportDate(LocalDate.of(2022, 3, 22))
                .build());

        TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
        searchCriteria.setTransportDate(LocalDate.of(2022, 3, 21));
        var results = searchOffers(searchCriteria);
        assertThat(results).containsExactly(transportOffer1);
    }

    @Test
    void shouldReturnPageOfData() {
        postOffer(aTransportOffer().title("a").build());
        postOffer(aTransportOffer().title("b").build());
        postOffer(aTransportOffer().title("c").build());
        postOffer(aTransportOffer().title("d").build());
        postOffer(aTransportOffer().title("e").build());
        postOffer(aTransportOffer().title("f").build());

        PageRequest page = PageRequest.of(1, 2);
        var results = searchOffers(page);

        assertThat(results.totalElements).isEqualTo(6);
        assertThat(results.content)
                .hasSize(2)
                .extracting(r -> r.title)
                .containsExactly("c", "d");
    }

    @Test
    void shouldSortResults() {
        postOffer(aTransportOffer().title("a").build());
        postOffer(aTransportOffer().title("bb").build());
        postOffer(aTransportOffer().title("bą").build());
        postOffer(aTransportOffer().title("c").build());
        postOffer(aTransportOffer().title("ć").build());
        postOffer(aTransportOffer().title("d").build());

        PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"));
        var results = searchOffers(page);

        assertThat(results.content)
                .extracting(r -> r.title)
                .containsExactly("d", "ć", "c", "bb", "bą", "a");
    }

    @Nested
    class ValidationTest {

        @Test
        void shouldRejectNullCapacity() {
            TransportOffer offer = sampleOfferRequest();
            offer.capacity = null;
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0, 100, 101, 1000})
        void shouldRejectIncorrectCapacity(int capacity) {
            TransportOffer offer = sampleOfferRequest();
            offer.capacity = capacity;
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldRejectNullOrigin() {
            TransportOffer offer = sampleOfferRequest();
            offer.origin = null;
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldRejectNullDestination() {
            TransportOffer offer = sampleOfferRequest();
            offer.destination = null;
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

    }

    private List<TransportOffer> searchOffers(TransportOfferSearchCriteria searchCriteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/" + getOfferSuffix());
        if (searchCriteria.getOrigin() != null) {
            builder
                    .queryParam("origin.city", searchCriteria.getOrigin().getCity())
                    .queryParam("origin.region", searchCriteria.getOrigin().getRegion());
        }
        if (searchCriteria.getDestination() != null) {
            builder
                    .queryParam("destination.city", searchCriteria.getDestination().getCity())
                    .queryParam("destination.region", searchCriteria.getDestination().getRegion());
        }
        if (searchCriteria.getCapacity() != null) {
            builder.queryParam("capacity", searchCriteria.getCapacity());
        }
        if (searchCriteria.getTransportDate() != null) {
            builder.queryParam("transportDate", searchCriteria.getTransportDate());
        }
        String url = builder.encode().toUriString();

        return listOffers(URI.create(url)).content;
    }

    private Offers<TransportOffer> searchOffers(PageRequest pageRequest) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/" + getOfferSuffix());
        builder.queryParam("page", pageRequest.getPageNumber());
        builder.queryParam("size", pageRequest.getPageSize());
        pageRequest.getSort().forEach(sort -> {
            builder.queryParam("sort", "%s,%s".formatted(sort.getProperty(), sort.getDirection()));
        });
        String url = builder.encode().toUriString();

        return listOffers(URI.create(url));
    }

    private TransportOfferBuilder aTransportOffer() {
        return TransportResourceTest.builder()
                .title("some title")
                .description("some description")
                .capacity(1)
                .origin(new Location("mazowieckie", "warszawa"))
                .destination(new Location("pomorskie", "gdańsk"))
                .transportDate(LocalDate.now())
                ;
    }

    @Builder
    private static TransportOffer transportOfferBuilder(
            String title,
            String description,
            Location origin,
            Location destination,
            Integer capacity,
            LocalDate transportDate
    ) {
        TransportOffer result = new TransportOffer();
        result.title = Optional.ofNullable(title).orElse("some title");
        result.description = Optional.ofNullable(description).orElse("some description");
        result.origin = origin;
        result.destination = destination;
        result.capacity = capacity;
        result.transportDate = transportDate;
        return result;
    }
}
