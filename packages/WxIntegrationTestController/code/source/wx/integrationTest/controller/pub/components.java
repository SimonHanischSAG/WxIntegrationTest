package wx.integrationTest.controller.pub;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.ISRuntimeException;
import com.wm.app.b2b.server.invoke.InvokeManager;
// --- <<IS-END-IMPORTS>> ---

public final class components

{
	// ---( internal utility methods )---

	final static components _instance = new components();

	static components _newInstance() { return new components(); }

	static components _cast(Object o) { return (components)o; }

	// ---( server methods )---




	public static final void endTest (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(endTest)>> ---
		// @sigtype java 3.5
		// [i] field:0:required testId
		// [i] field:0:required testRunId
		// [i] record:0:required pipeline
		try {
			pipeline = Service.doInvoke("wx.integrationTest.controller.impl", "endTestFlow", pipeline);
		} catch (ServiceException e) {
			throw e;
		} catch (ISRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
			
		// --- <<IS-END>> ---

                
	}



	public static final void prepareJMSMessageTest (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(prepareJMSMessageTest)>> ---
		// @sigtype java 3.5
		// [i] field:0:required testId
		// [i] record:0:required JMSMessage
		// [o] record:0:required JMSMessageInput
		// [o] field:0:required testRunId
		try {
			pipeline = Service.doInvoke("wx.integrationTest.controller.impl", "prepareJMSMessageTestFlow",
					pipeline);
		} catch (ServiceException e) {
			throw e;
		} catch (ISRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
			
		// --- <<IS-END>> ---

                
	}
}

