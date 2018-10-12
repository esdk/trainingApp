package de.abas.esdk.app.training;

import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.ButtonEventHandler;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.type.ButtonEventType;
import de.abas.erp.db.DbContext;
import de.abas.erp.db.infosystem.custom.ow1.TrainingTest;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;
import de.abas.esdk.client.api.license.LicenseChecker;

@EventHandler(head = TrainingTest.class, row = TrainingTest.Row.class)
@RunFopWith(EventHandlerRunner.class)
public class Main {

	@ButtonEventHandler(field = "start", type = ButtonEventType.AFTER)
	public void startAfter(DbContext ctx, TrainingTest infosys) {
		boolean result = LicenseChecker.instance().validate();

		ctx.out().println("License check passed: " + result);
	}

}
