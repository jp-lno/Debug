import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.naming.directory.Attributes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.query.LdapQuery;

@ExtendWith(MockitoExtension.class)
public class LdapUtilsTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private LdapConfigParams ldapConfigParams;

    @Mock
    private LdapQueryBuilder queryBuilder;

    private static final String USERNAME = "testUser";
    private static final String DISTINGUISHED_NAME = "cn=testUser,dc=example,dc=com";

    @BeforeEach
    void setUp() {
        when(ldapConfigParams.getGroupBase()).thenReturn("ou=groups");
        when(ldapConfigParams.getQueryAttributes()).thenReturn(new QueryAttributes());
        when(ldapConfigParams.getUserBases()).thenReturn(new String[]{"ou=users"});
        when(ldapConfigParams.getPrefix()).thenReturn("prefix");
        when(ldapConfigParams.getSuffix()).thenReturn("suffix");
    }

    @Test
    void testSearchGroupsByUsernameFiltered() {
        List<String> groups = List.of("prefixGroup1suffix", "prefixGroup2suffix", "Group3");

        // Mock the searchGroupsByUsername method
        try (MockedStatic<LdapUtils> mockedLdapUtils = mockStatic(LdapUtils.class)) {
            mockedLdapUtils.when(() -> LdapUtils.searchGroupsByUsername(any(), any(), any()))
                    .thenReturn(groups);

            List<String> filteredGroups = LdapUtils.searchGroupsByUsernameFiltered(ldapTemplate, ldapConfigParams, USERNAME);

            assertEquals(2, filteredGroups.size());
            assertTrue(filteredGroups.contains("prefixGroup1suffix"));
            assertTrue(filteredGroups.contains("prefixGroup2suffix"));
        }
    }

    @Test
    void testSearchGroupsByUsername() {
        List<String> groups = List.of("group1", "group2");

        try (MockedStatic<LdapUtils> mockedLdapUtils = mockStatic(LdapUtils.class)) {
            mockedLdapUtils.when(() -> LdapUtils.getDistinguishedName(any(), any(), any()))
                    .thenReturn(DISTINGUISHED_NAME);
            mockedLdapUtils.when(() -> LdapUtils.searchGroupsOfDistinguishedName(any(), any(), any()))
                    .thenReturn(groups);

            List<String> result = LdapUtils.searchGroupsByUsername(ldapTemplate, ldapConfigParams, USERNAME);

            assertEquals(groups, result);
        }
    }

    @Test
    void testSearchGroupsByUsernameWithNoDistinguishedName() {
        try (MockedStatic<LdapUtils> mockedLdapUtils = mockStatic(LdapUtils.class)) {
            mockedLdapUtils.when(() -> LdapUtils.getDistinguishedName(any(), any(), any()))
                    .thenReturn(null);

            List<String> result = LdapUtils.searchGroupsByUsername(ldapTemplate, ldapConfigParams, USERNAME);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testGetDistinguishedName() {
        List<String> distinguishedNames = List.of(DISTINGUISHED_NAME);

        when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class)))
                .thenReturn(distinguishedNames);

        String result = LdapUtils.getDistinguishedName(ldapTemplate, ldapConfigParams, USERNAME);

        assertEquals(DISTINGUISHED_NAME, result);
    }

    @Test
    void testGetDistinguishedNameNotFound() {
        when(ldapTemplate.search(any(LdapQuery.class), any(AttributesMapper.class)))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ForbiddenException.class, () ->
                LdapUtils.getDistinguishedName(ldapTemplate, ldapConfigParams, USERNAME));

        assertTrue(exception.getMessage().contains("Username not found"));
    }
}
