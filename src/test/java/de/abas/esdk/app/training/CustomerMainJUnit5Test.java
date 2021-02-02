package de.abas.esdk.app.training;

import org.junit.jupiter.api.Test;

public class CustomerMainJUnit5Test {

	@Test
	public void checkTest() {
		CustomerMain customerMain = new CustomerMain();
		assert customerMain.check("Jasmin Börsig");
	}

}
