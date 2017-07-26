package de.abas.esdk.app.training;

import de.abas.erp.axi.event.EventException;
import de.abas.erp.axi2.EventHandlerRunner;
import de.abas.erp.axi2.annotation.EventHandler;
import de.abas.erp.axi2.annotation.FieldEventHandler;
import de.abas.erp.axi2.type.FieldEventType;
import de.abas.erp.db.schema.customer.CustomerEditor;
import de.abas.erp.jfop.rt.api.annotation.RunFopWith;

@EventHandler(head = CustomerEditor.class)
@RunFopWith(EventHandlerRunner.class)
public class CustomerMain {

	@FieldEventHandler(field = "contactPerson", type = FieldEventType.VALIDATION)
	public void contactPersonValidation(CustomerEditor head) throws EventException {
		if (!check(head.getContactPerson())) {
			throw new EventException("Field value invalid!");
		}
	}

	boolean check(String contactPerson) {
		return contactPerson.matches("[A-ZÄÖÜ][A-Za-zÄÖÜäöü]+ [A-ZÄÖÜ][A-Za-zÄÖÜäöü]+");
	}

}
