package de.abas.esdk.app.training;

import org.junit.Test;

public class CustomerMainTest {

	@Test
	public void checkTest() {
		CustomerMain customerMain = new CustomerMain();
		assert customerMain.check("Jasmin Börsig");
	}

}