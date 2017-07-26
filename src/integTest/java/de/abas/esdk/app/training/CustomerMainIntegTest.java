package de.abas.esdk.app.training;

import de.abas.erp.db.DbContext;
import de.abas.erp.db.exception.DBRuntimeException;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.db.util.ContextHelper;
import org.junit.Test;
import org.w3c.dom.events.EventException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.fail;

public class CustomerMainIntegTest {

	String hostname = "";
	String client = "";
	int port = 6550;
	String password = "";

	@Test
	public void customerContactPersonTest() {
		loadProperties();
		try {
			DbContext ctx = ContextHelper.createClientContext(hostname, port, client, password, "Test");
			CustomerEditor customerEditor = ctx.newObject(CustomerEditor.class);
			customerEditor.setContactPerson("blub");
			fail("EventException expected");
		} catch (DBRuntimeException e) {
			assert e.getMessage().contains("Field value invalid!");
		}
	}

	private void loadProperties() {
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

}
