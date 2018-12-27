package me.marques.anderson;

import io.vertx.core.json.JsonObject;
import me.marques.anderson.domain.Service;
import me.marques.anderson.domain.Toggle;
import me.marques.anderson.domain.ToggleUser;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToggleTest {

    private final Toggle subject = new Toggle("some-toggle", true);

    @Test
    public void toggleDefaultBehaviorTest() {
        assertFalse(subject.isRestricted());
    }

    @Test
    public void toggleOpenToAllServicesTest() {
        assertTrue(subject.canBeUsedByAllServices());
    }

    @Test
    public void toggleIsRestrictedTest() {
        subject.addAudience(new ToggleUser("serviceA", 2));

        assertTrue(subject.isRestricted());
    }

    @Test
    public void restrictedToggleAllowedToAudienceTest() {
        Service serviceA = new ToggleUser("serviceA", 1);
        Service serviceC = new ToggleUser("serviceC", 2);

        subject.addAudience(serviceA);
        subject.addAudience(serviceC);

        assertTrue(subject.canBeUsedBy("serviceA"));
        assertTrue(subject.canBeUsedBy("serviceC"));
        assertTrue(subject.cannotBeUsedBy("serviceB"));
    }

    @Test
    public void restrictedToggleNotAllowedToOtherServicesTest() {
        subject.addAudience(new ToggleUser("serviceA", 1));

        assertTrue(subject.cannotBeUsedBy("serviceB"));
        assertTrue(subject.cannotBeUsedBy("serviceC"));
    }

    @Test
    public void toggleDeniedToServiceTest() {
        Service serviceA = new ToggleUser("serviceA", 2);

        subject.denyUseTo(serviceA);

        assertTrue(subject.cannotBeUsedBy("serviceA"));
    }

    @Test
    public void toggleDeniedToServiceOpenToOthersTest() {
        Service serviceA = new ToggleUser("serviceA", 2);

        subject.denyUseTo(serviceA);

        assertTrue(subject.cannotBeUsedBy("serviceA"));
        assertTrue(subject.canBeUsedBy("serviceB"));
        assertTrue(subject.canBeUsedBy("serviceC"));
    }

    @Test
    public void defaultValueTest() {
        Boolean defaultValue = subject.getDefaultValue();

        assertEquals(defaultValue, subject.getUserValue("serviceA", 1));
    }


    @Test
    public void overriddenValueTest() {
        ToggleUser serviceA = new ToggleUser("serviceA", 2, false);

        Boolean defaultValue = subject.getDefaultValue();

        subject.setUserValue(serviceA);

        assertNotEquals(defaultValue, subject.getUserValue("serviceA", 2));
    }

    @Test
    public void jsonParsingTest() {
        Toggle toggle = new Toggle("toggleA", false);
        toggle.addAudience(new ToggleUser("serviceA", 1));
        toggle.addAudience(new ToggleUser("serviceB", 3));
        toggle.denyUseTo(new ToggleUser("serviceC", 3));
        toggle.setUserValue(new ToggleUser("serviceD", 4, true));

        JsonObject jsonObject = toggle.toJson();

        Toggle rebuildedToggle = Toggle.createFromJson(jsonObject);

        assertNotNull(rebuildedToggle);
        assertEquals("toggleA", rebuildedToggle.getName());
        assertEquals(false, rebuildedToggle.getDefaultValue());
        assertTrue(rebuildedToggle.getAudience().contains("serviceA"));
        assertTrue(rebuildedToggle.getAudience().contains("serviceB"));
        assertTrue(rebuildedToggle.getBlacklist().contains("serviceC"));
        assertTrue(rebuildedToggle.getUserValue("serviceD", 4));
    }
}
