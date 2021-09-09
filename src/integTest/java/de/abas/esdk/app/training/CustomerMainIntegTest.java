package de.abas.esdk.app.training;

import de.abas.erp.db.exception.DBRuntimeException;
import de.abas.erp.db.infosystem.custom.ow1.TrainingTest;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.esdk.test.util.DoNotFailOnError;
import de.abas.esdk.test.util.EsdkIntegTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class CustomerMainIntegTest extends EsdkIntegTest {

    private static final String FIELD_VALUE_ERROR_MESSAGE = "Field value invalid!";
    private static final String FIELD_VALUE_ERROR = "ERROR_MESSAGE Cat=ERROR Fld=contactPerson: " + FIELD_VALUE_ERROR_MESSAGE;

    private CustomerEditor customerEditor = null;

    @Before
    public void printErpVersion() {
        System.out.println("ERP_VERSION from environment: " + System.getenv("ERP_VERSION"));
    }

    @DoNotFailOnError(message = FIELD_VALUE_ERROR)
    @Test
    public void customerContactPersonTest() {
        try {
            customerEditor = ctx.newObject(CustomerEditor.class);
            customerEditor.setContactPerson("blub");
            fail("DBRuntimeException expected");
        } catch (DBRuntimeException e) {
            assertThat(e.getMessage(), containsString(FIELD_VALUE_ERROR_MESSAGE));
            assertThat(getErrors(), contains(FIELD_VALUE_ERROR));
        }
    }

    @Test
    public void infosystemEventHandlerPassesLicenseCheck() {
        TrainingTest trainingTest = ctx.openInfosystem(TrainingTest.class);
        trainingTest.invokeStart();
        assertThat(getMessages(), hasItem("TEXT_MESSAGE: I produce some output."));
    }

    @After
    public void cleanup() {
        if (customerEditor != null && customerEditor.active()) {
            customerEditor.abort();
        }
    }

}
