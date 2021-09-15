package wx.integrationTest.controller.impl;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void dropPipelineVariable (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(dropPipelineVariable)>> ---
		// @sigtype java 3.5
		// [i] field:0:required variableName
		IDataMap pMap = new IDataMap(pipeline);
		String variableName = pMap.getAsString("variableName");
		pMap.remove(variableName);
		
		// --- <<IS-END>> ---

                
	}



	public static final void notifyPollingService (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(notifyPollingService)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [i] record:0:required pipeline
		IDataMap pipeMap = new IDataMap(pipeline);
		String id = pipeMap.getAsString("id");
		if (id == null) {
			throw new ServiceException("notifyPollingService: id is empty");
		}
		IData genericResponse = pipeMap.getAsIData("genericResponse");
		// log("SITC: " + id);
		// log("SITC: length of arrivedResponses before notifying:" +
		// arrivedResponses.size());
		
		cleanUpArrivedResponses();
		
		// log("SITC: before put :" + id + "," + arrivedResponses);
		Response response = new Response();
		response.date = new Date();
		response.genericResponse = genericResponse;
		arrivedResponses.put(id, response);
		
		// log("SITC: length of arrivedResponses after notifying:" +
		// arrivedResponses.size());
		
		// --- <<IS-END>> ---

                
	}



	public static final void pollForMessage (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(pollForMessage)>> ---
		// @sigtype java 3.5
		// [i] field:0:required id
		// [o] record:0:optional pipeline
		// log("SITC: length of arrivedResponses before polling:" +
		// arrivedResponses.size());
		
		IDataMap pipeMap = new IDataMap(pipeline);
		String id = pipeMap.getAsString("id");
		if (id == null) {
			throw new ServiceException("pollForMessage: id is empty");
		}
		
		String timeoutInSeconds = pipeMap.getAsString("timeoutInSeconds");
		int myPollingCountMax = pollingCountMax;
		if (timeoutInSeconds != null && !timeoutInSeconds.equals("")) {
			myPollingCountMax = Integer.valueOf(timeoutInSeconds);
		}
		
		Date foundAndRemovedValue = null;
		int pollingCount = 0;
		do {
			try {
				Thread.sleep(pollingInterval);
			} catch (InterruptedException e) {
			}
			// log("SITC: length of arrivedResponses during polling:" +
			// arrivedResponses.size());
			// log("SITC: check for " + id + " in arrivedResponses");
			Response response = arrivedResponses.remove(id);
			// log("SITC: response " + response);
			if (response != null) {
				// log("SITC: response not null");
				foundAndRemovedValue = response.date;
				if (response.genericResponse != null) {
					pipeMap.put("genericResponse", response.genericResponse);
				}
			}
			pollingCount++;
		} while (foundAndRemovedValue == null && pollingCount < myPollingCountMax);
		
		if (foundAndRemovedValue == null) {
			throw new ServiceException("pollForMessage: Timeout during polling for response for id: " + id);
		}
		// log("SITC: length of arrivedResponses after polling:" +
		// arrivedResponses.size());
		
		// --- <<IS-END>> ---

                
	}



	public static final void replacePipeline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(replacePipeline)>> ---
		// @sigtype java 3.5
		// [i] record:0:required compensatingPipeline
		IDataMap pipeMap = new IDataMap(pipeline);
		IData compensatingPipeline = pipeMap.getAsIData("compensatingPipeline");
		
		IDataMap compensatingPipeMap = new IDataMap(compensatingPipeline);
		
		pipeMap.clear();
		
		Set<String> keys = compensatingPipeMap.keySet();
		for (String key : keys) {
			pipeMap.put(key, compensatingPipeMap.get(key));
		}
		
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	
	private static long pollingInterval = 1000;
	private static int pollingCountMax = 20;
	private static ConcurrentHashMap<String, Response> arrivedResponses = new ConcurrentHashMap<String, Response>();
	
	private static void cleanUpArrivedResponses() {
		// log("SITC: start cleanup");
		long currentDate = new Date().getTime();
		Set<Entry<String, Response>> set = arrivedResponses.entrySet();
		for (Entry<String, Response> entry : set) {
			if ((currentDate - entry.getValue().date.getTime()) > 2 * pollingInterval) {
				arrivedResponses.remove(entry.getKey());
				// log("SITC: removed");
			}
		}
		// log("SITC: end cleanup");
	}
	
	private static class Response {
	
		private Date date;
		private IData genericResponse;
	
	}
	
	public static void log(String message) {
		// input
		IData input = IDataFactory.create();
		IDataCursor inputCursor = input.getCursor();
		IDataUtil.put(inputCursor, "message", message);
		inputCursor.destroy();
	
		try {
			Service.doInvoke("pub.flow", "debugLog", input);
		} catch (Exception e) {
		}
	}
	
	// --- <<IS-END-SHARED>> ---
}

