package pl.gov.coi.pomocua.ads.users;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.gov.coi.pomocua.ads.TestConfiguration;
import pl.gov.coi.pomocua.ads.UserId;
import pl.gov.coi.pomocua.ads.authentication.TestCurrentUser;
import pl.gov.coi.pomocua.ads.users.UsersResource.UserInfo;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfiguration.class)
public class UserResourceTest {
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected TestCurrentUser testCurrentUser;

    @Autowired TestUsersRepository testUsersRepository;

    @AfterEach
    void clear() {
        testUsersRepository.clear();
    }

    @Test
    void shouldReturnCurrentUserData() {
        User user = new User(new UserId("some-current-id"), "some@email.invalid", "600000000");
        testUsersRepository.saveUser(user);
        testCurrentUser.setCurrentUserId(user.id());

        ResponseEntity<UserInfo> response = restTemplate.getForEntity("/api/secure/me", UserInfo.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email).isEqualTo("some@email.invalid");
        assertThat(response.getBody().phoneNumber).isEqualTo("600000000");
    }

    @Test
    void handleMissingUser() {
        User user = new User(new UserId("some-current-id"), "some@email.invalid", "600000000");
        testUsersRepository.clear();
        testCurrentUser.setCurrentUserId(user.id());

        ResponseEntity<UserInfo> response = restTemplate.getForEntity("/api/secure/me", UserInfo.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
