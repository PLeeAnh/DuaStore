package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Cac tinh huong thuc te lien quan den fix bao mat: 2FA truoc day chi chan
 * "/admin/**" nen mot admin bi lo cookie remember-me van co the mo ket noi
 * WebSocket ("/ws/**") ma khong can xac thuc buoc 2. Filter hien tai phai
 * chan ca hai nhom duong dan.
 */
class TwoFactorAuthFilterTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TwoFactorAuthFilter filter = new TwoFactorAuthFilter(userRepository);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void nonAdminUser_hittingAdminPath_isNotGated() throws Exception {
        // Spring Security's own authorization rules would normally block a USER
        // role from /admin/** before this filter runs; this filter must not add
        // its own (incorrect) gate on top for non-admin roles.
        authenticateAs("nguyenvan", "ROLE_USER");
        HttpServletRequest request = request("/admin/don-hang");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void admin_withTwoFactorDisabled_isNotGated() throws Exception {
        authenticateAs("admin", "ROLE_ADMIN");
        when(userRepository.findByUsernameOrEmail("admin"))
                .thenReturn(java.util.Optional.of(userWith2fa(false)));
        HttpServletRequest request = requestWithSession("/admin/don-hang", null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void admin_rememberMeLogin_twoFactorEnabledButNeverChallenged_redirectsToChallenge() throws Exception {
        // Remember-me auto-login never sets the "2faUserId" session attribute
        // (only the interactive login handler does), so the filter must fall
        // back to looking the user up directly.
        authenticateAs("admin", "ROLE_ADMIN");
        when(userRepository.findByUsernameOrEmail("admin"))
                .thenReturn(java.util.Optional.of(userWith2fa(true)));
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = requestWithSession("/admin/don-hang", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(response).sendRedirect("/admin/2fa/challenge");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void admin_websocketPath_withUnverified2fa_isAlsoGated() throws Exception {
        // This is the exact bug fixed this session: previously only
        // path.startsWith("/admin/") was checked, so "/ws/**" bypassed 2FA
        // entirely for an admin whose session was never challenged.
        authenticateAs("admin", "ROLE_ADMIN");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("2faUserId")).thenReturn(1);
        when(session.getAttribute("2faVerified")).thenReturn(false);
        HttpServletRequest request = requestWithSession("/ws/admin-notifications", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(response).sendRedirect("/admin/2fa/challenge");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void admin_websocketPath_alreadyVerifiedWithinTimeout_proceeds() throws Exception {
        authenticateAs("admin", "ROLE_ADMIN");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("2faUserId")).thenReturn(1);
        when(session.getAttribute("2faVerified")).thenReturn(true);
        when(session.getAttribute("2faVerifiedAt")).thenReturn(Instant.now().minusSeconds(60).toString());
        HttpServletRequest request = requestWithSession("/ws/admin-notifications", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void admin_verifiedButSessionTimedOutAfter60Minutes_reChallenges() throws Exception {
        authenticateAs("admin", "ROLE_ADMIN");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("2faUserId")).thenReturn(1);
        when(session.getAttribute("2faVerified")).thenReturn(true);
        when(session.getAttribute("2faVerifiedAt"))
                .thenReturn(Instant.now().minusSeconds(61 * 60).toString());
        HttpServletRequest request = requestWithSession("/admin/don-hang", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getContextPath()).thenReturn("");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(session).removeAttribute("2faVerified");
        verify(session).removeAttribute("2faVerifiedAt");
        verify(response).sendRedirect("/admin/2fa/challenge");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void admin_hittingChallengePageItself_isNeverGated() throws Exception {
        // Otherwise an admin who needs to verify could never reach the
        // challenge page in the first place (infinite redirect loop).
        authenticateAs("admin", "ROLE_ADMIN");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("2faUserId")).thenReturn(1);
        when(session.getAttribute("2faVerified")).thenReturn(false);
        HttpServletRequest request = requestWithSession("/admin/2fa/challenge", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void admin_browsingClientFacingPage_isNotGated() throws Exception {
        // /san-pham is a normal storefront page an admin might browse while
        // logged in; 2FA must only gate admin-console and websocket paths.
        authenticateAs("admin", "ROLE_ADMIN");
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("2faUserId")).thenReturn(1);
        when(session.getAttribute("2faVerified")).thenReturn(false);
        HttpServletRequest request = requestWithSession("/san-pham/7", session);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(any());
    }

    private void authenticateAs(String username, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User userWith2fa(boolean enabled) {
        User u = new User();
        u.setId(1);
        u.setTwoFactorEnabled(enabled);
        return u;
    }

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getSession(false)).thenReturn(null);
        return request;
    }

    private HttpServletRequest requestWithSession(String uri, HttpSession session) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
        return request;
    }
}
