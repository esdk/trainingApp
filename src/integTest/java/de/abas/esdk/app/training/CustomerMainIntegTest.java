package de.abas.esdk.app.training;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.DbMessage;
import de.abas.erp.db.MessageListener;
import de.abas.erp.db.exception.DBRuntimeException;
import de.abas.erp.db.infosystem.custom.ow1.TrainingTest;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.db.util.ContextHelper;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.events.EventException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CustomerMainIntegTest {

	private static String hostname = "";
	private static String client = "";
	private static int port = 6550;
	private static String password = "";

	@Test
	public void customerContactPersonTest() {
		try {
			DbContext ctx = getContext();
			CustomerEditor customerEditor = ctx.newObject(CustomerEditor.class);
			customerEditor.setContactPerson("blub");
			fail("EventException expected");
		} catch (DBRuntimeException e) {
			assert e.getMessage().contains("Field value invalid!");
		}
	}

	@Test
	public void infosystemEventHandlerPassesLicenseCheck() {
		DbContext ctx = getContext();
		List<String> messages = new LinkedList<>();
		ctx.addMessageListener(dbMessage -> messages.add(dbMessage.toString()));
		TrainingTest trainingTest = ctx.openInfosystem(TrainingTest.class);
		trainingTest.invokeStart();
		assertThat(messages, hasItem("TEXT_MESSAGE: License check passed: true"));
	}

	@BeforeClass
	public static void loadProperties() {
		final Properties pr = new Properties();
		final File configFile = new File("gradle.properties");
		try {
			pr.load(new FileReader(configFile));
			hostname = pr.getProperty("EDP_HOST");
			client = pr.getProperty("EDP_CLIENT");
			port = Integer.parseInt(pr.getProperty("EDP_PORT", "6550"));
			password = pr.getProperty("EDP_PASSWORD");
		} catch (final FileNotFoundException e) {
			throw new RuntimeException("Could not find configuration file " + configFile.getAbsolutePath());
		} catch (final IOException e) {
			throw new RuntimeException("Could not load configuration file " + configFile.getAbsolutePath());
		}
	}

	private DbContext getContext() {
		return ContextHelper.createClientContext(hostname, port, client, password, "Test");
	}
}
