package de.abas.esdk.app.training;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.esdk.test.util.ServerSideErrorMessageException;
import de.abas.esdk.test.util.TestSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class CustomerMainJUnit5IntegTest {

    private static final String FIELD_VALUE_ERROR_MESSAGE = "Field value invalid!";

    private DbContext ctx;
    private CustomerEditor customerEditor = null;

    @BeforeEach
    private void setupContext() {
        ctx = TestSetup.createClientContext();
        TestSetup.listenToServerSideErrors(ctx);
    }

    @AfterEach
    private void cleanup() {
        if (customerEditor != null && customerEditor.active()) {
            customerEditor.abort();
        }
        ctx.close();
    }

    @Test
    public void customerContactPersonTest() {
        try {
            customerEditor = ctx.newObject(CustomerEditor.class);
            customerEditor.setContactPerson("blub");
        } catch (ServerSideErrorMessageException e) {
            assertThat(e.getMessage(), containsString(FIELD_VALUE_ERROR_MESSAGE));
        }
    }
}
