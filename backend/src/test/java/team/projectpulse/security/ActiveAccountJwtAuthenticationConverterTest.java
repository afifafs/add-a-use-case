package team.projectpulse.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.student.Student;
import team.projectpulse.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ActiveAccountJwtAuthenticationConverterTest {

    @Mock
    UserRepository userRepository;

    private Jwt tokenFor(Integer userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("student@example.edu")
                .claim("userId", userId)
                .claim("authorities", "ROLE_student")
                .build();
    }

    @Test
    void authenticatesAnEnabledAccountAndPreservesItsAuthorities() {
        Student student = new Student("student", "First", "Last", "student@example.edu", "encoded", true, "student");
        given(this.userRepository.findById(7)).willReturn(Optional.of(student));

        var authentication = new ActiveAccountJwtAuthenticationConverter(this.userRepository).convert(tokenFor(7));

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_student");
    }

    @Test
    void rejectsAnAccountDisabledAfterItsTokenWasIssued() {
        Student student = new Student("student", "First", "Last", "student@example.edu", "encoded", false, "student");
        given(this.userRepository.findById(7)).willReturn(Optional.of(student));

        assertThatThrownBy(() -> new ActiveAccountJwtAuthenticationConverter(this.userRepository).convert(tokenFor(7)))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void rejectsDisabledInstructorAccountsUnderTheSharedAccountRule() {
        Instructor instructor = new Instructor("instructor", "First", "Last", "instructor@example.edu", "encoded", false, "instructor");
        given(this.userRepository.findById(7)).willReturn(Optional.of(instructor));

        assertThatThrownBy(() -> new ActiveAccountJwtAuthenticationConverter(this.userRepository).convert(tokenFor(7)))
                .isInstanceOf(InvalidBearerTokenException.class);
    }

    @Test
    void rejectsAnAccountThatNoLongerExists() {
        given(this.userRepository.findById(7)).willReturn(Optional.empty());

        assertThatThrownBy(() -> new ActiveAccountJwtAuthenticationConverter(this.userRepository).convert(tokenFor(7)))
                .isInstanceOf(InvalidBearerTokenException.class);
    }
}
