package com.backbase.dbs.notifications;

import static java.util.Collections.singletonList;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.backbase.dbs.notifications.rest.spec.v2.notifications.NotificationsPostRequestBody;
import com.backbase.dbs.notifications.rest.spec.v2.notifications.Recipient;
import com.backbase.dbs.notifications.rest.spec.v2.notifications.SeverityLevel;
import com.backbase.dbs.presentation.services.notifications.Application;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {
        Application.class
    })
@DirtiesContext
@ActiveProfiles("it")
@TestPropertySource(properties =
    {
        "spring.config.additional-location=classpath:/apiExtension.yml",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration",
        "backbase.access-control.enabled=false",
        "backbase.notifications.approval.enabled=false",
    })
@TestExecutionListeners({
    DirtiesContextTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DbUnitTestExecutionListener.class,
    WithSecurityContextTestExecutionListener.class
})
public class NotificationsAdditionsTest {

    private static final String EMPLOYEE_NOTIFICATIONS_ROOT = "/client-api/v2/employee/notifications";

    @Autowired
    WebApplicationContext wac;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mvc;

    @BeforeClass
    public static void setEnvironmentVariables() {
        System.setProperty("SIG_SECRET_KEY", "JwtSecretKeyPleaseDoNotUseInProduction!");
    }

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "bbuser")
    @ExpectedDatabase(value = "classpath:expected-extended-additions.xml",
        assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void shouldCreateNotificationWithCorrectAdditions() throws Exception {
        Map<String, String> additions = new HashMap<>();
        additions.put("textColor", "blue");
        additions.put("backgroundColor", "red");
        NotificationsPostRequestBody requestBody = createNotificationsPostRequestBody();
        requestBody.setAdditions(additions);

        mvc.perform(post(EMPLOYEE_NOTIFICATIONS_ROOT)
            .content(objectMapper.writeValueAsBytes(requestBody))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(username = "bbuser")
    @ExpectedDatabase(value = "classpath:empty.xml",
        assertionMode = DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void shouldNotCreateNotificationWithNotConfiguredAdditions() throws Exception {
        Map<String, String> additions = new HashMap<>();
        additions.put("incorrectAddition", "blue");
        NotificationsPostRequestBody requestBody = createNotificationsPostRequestBody();
        requestBody.setAdditions(additions);

        mvc.perform(post(EMPLOYEE_NOTIFICATIONS_ROOT)
            .content(objectMapper.writeValueAsBytes(requestBody))
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    private NotificationsPostRequestBody createNotificationsPostRequestBody() {
        return new NotificationsPostRequestBody()
            .withMessage("Message")
            .withLevel(SeverityLevel.INFO)
            .withTargetGroup(NotificationsPostRequestBody.TargetGroup.USER)
            .withRecipients(singletonList(new Recipient().withUserId("U000011")))
            .withOrigin("actions");
    }
}
