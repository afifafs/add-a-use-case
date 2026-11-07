package team.projectpulse.security.authorizationmanagers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import team.projectpulse.student.StudentSecurityService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class StudentLifecycleAuthorizationManagerTest {

    @Mock
    StudentSecurityService studentSecurityService;

    @InjectMocks
    StudentLifecycleAuthorizationManager authorizationManager;

    @Test
    void delegatesTheStudentIdBoundByTheRoute() {
        given(this.studentSecurityService.canManageLifecycle(7)).willReturn(true);

        AuthorizationResult result = this.authorizationManager.authorize(() -> null,
                new RequestAuthorizationContext(mock(HttpServletRequest.class), Map.of("studentId", "7")));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isTrue();
    }

    @Test
    void deniesAndDoesNotQueryWhenTheRouteDidNotBindAStudentId() {
        AuthorizationResult result = this.authorizationManager.authorize(() -> null,
                new RequestAuthorizationContext(mock(HttpServletRequest.class), Map.of()));

        assertThat(result).isNotNull();
        assertThat(result.isGranted()).isFalse();
        verifyNoInteractions(this.studentSecurityService);
    }
}
