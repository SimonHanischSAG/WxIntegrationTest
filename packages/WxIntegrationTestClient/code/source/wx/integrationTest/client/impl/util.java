package wx.integrationTest.client.impl;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.Server;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import com.softwareag.util.IDataMap;
import com.softwareag.wx.is.interceptor.filter.FilterUtil;
import com.softwareag.wx.is.interceptor.filter.SuperIDataMap;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void checkFilterExpression (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(checkFilterExpression)>> ---
		// @sigtype java 3.5
		// [i] field:0:required filter
		// [i] record:0:required pipeline
		// [o] field:0:required matches
		// [o] field:0:required message
		IDataMap pMap = new IDataMap(pipeline);
		IData pipelineFromPipeline = pMap.getAsIData("pipeline");
		String message = "";
		String matches = "false";
		String superIDataMap = "";
		
		String filter = "pipeline." + pMap.getAsString("filter");
		
		try {
			JexlExpression jexlExpression = FilterUtil.getJexlEngine().createExpression(filter);
		
			JexlContext jc = new MapContext();
			SuperIDataMap sm = new SuperIDataMap(pipelineFromPipeline);
			jc.set("pipeline", sm);
		
			superIDataMap = sm.toString();
		
			Object resultObject = jexlExpression.evaluate(jc);
			if (((resultObject instanceof Boolean)) && (((Boolean) resultObject).booleanValue() == true)) {
				message = "Found matching filter for pipeline. Internal filterexpression (without surrounding quotes): \""
						+ filter + "\"";
				matches = "true";
			}
		} catch (JexlException e) {
			message = "Could not create Filter Expression for internal filter statement (without surrounding quotes): \""
					+ filter + "\": " + e;
		}
		
		pMap.put("message", message);
		pMap.put("matches", matches);
		pMap.put("superIDataMap", superIDataMap);
		// --- <<IS-END>> ---

                
	}



	public static final void documentToString (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(documentToString)>> ---
		// @sigtype java 3.5
		// [i] record:0:required document
		// [i] field:0:optional keySorrtingLocale
		// [o] field:0:required string
		final IDataCursor crsr = pipeline.getCursor();
		final IData document = IDataUtil.getIData(crsr, "document");
		if (document == null) {
			throw new NullPointerException("Missing parameter: document");
		}
		final String keySortingLocaleStr = IDataUtil.getString(crsr, "keySortingLocale");
		final StringStreamer stringStreamer = new StringStreamer();
		if (keySortingLocaleStr != null  &&  keySortingLocaleStr.length() > 0) {
			Locale locale;
			Throwable th = null;
			try {
				locale = Locale.forLanguageTag(keySortingLocaleStr);
			} catch (Throwable t) {
				locale = null;
				th = t;
			}
			if (locale == null) {
				if (th == null) {
					throw new IllegalArgumentException("Invalid parameter keySortingLocale: " + keySortingLocaleStr);
				} else {
					throw new IllegalArgumentException("Invalid parameter keySortingLocale: " + keySortingLocaleStr, th);
				}
			} else {
				stringStreamer.setKeySortingLocale(locale);
			}
		}
		final StringBuilder sb = new StringBuilder();
		stringStreamer.toString(sb, document, true);
		IDataUtil.put(crsr, "string", sb.toString());
		crsr.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void getCurrentPipeline (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getCurrentPipeline)>> ---
		// @sigtype java 3.5
		// [i] field:0:required test
		// [o] record:0:required currentPipeline
		IDataCursor pipelineCursor = pipeline.getCursor();		
		try{
			IData currentPipeline = IDataUtil.deepClone(pipeline);
			IDataUtil.put( pipelineCursor, "currentPipeline", currentPipeline );
		} 
		catch( Exception e){			
		}		
		pipelineCursor.destroy();		
			
		// --- <<IS-END>> ---

                
	}



	public static final void getDifferenceOfPipelines (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getDifferenceOfPipelines)>> ---
		// @sigtype java 3.5
		// [i] record:0:required pipeline1
		// [i] record:0:required pipeline2
		// [o] record:0:required pipelineDifference
		IDataMap pMap = new IDataMap(pipeline);
		IData pipeline1 = pMap.getAsIData("pipeline1");
		IData pipeline2 = pMap.getAsIData("pipeline2");
		IDataMap pMap1 = new IDataMap(pipeline1);
		IDataMap pMap2 = new IDataMap(pipeline2);
		IData pipelineDifference = IDataFactory.create();
		IDataMap diffMap = new IDataMap(pipelineDifference);
		
		BiConsumer<String, Object> action = new BiConsumer<String, Object>() {
			public void accept(String key1, Object object1) {
				// Has pipeline2 the key, too?
				boolean pipeline2containsKey1 = pMap2.containsKey(key1);
				Object object2 = pMap2.get(key1);
				if (!pipeline2containsKey1 || object1 != null && object2 == null || object1 == null && object2 != null
						|| object1 != null && object2 != null && !object1.equals(object2)) {
					// pipeline2 has not the key -> add to difference:
					diffMap.put(key1, object1);
				}
			};
		
		};
		pMap1.forEach(action);
		
		pMap.put("pipelineDifference", pipelineDifference);
		// --- <<IS-END>> ---

                
	}



	public static final void getHostname (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getHostname)>> ---
		// @sigtype java 3.5
		// [o] field:0:required hostname
		String hostname = null;
		// pipeline
		
		try {
			hostname = InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
		} catch (Exception e) {
		}
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put(pipelineCursor, "result", hostname);
		pipelineCursor.destroy();
		
		// --- <<IS-END>> ---

                
	}



	public static final void getIntegrationServerPath (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getIntegrationServerPath)>> ---
		// @sigtype java 3.5
		// [o] field:0:required integrationServerPath
		IDataMap pipeMap = new IDataMap(pipeline);
		pipeMap.put("integrationServerPath", Server.getHomeDir().toString());
		// --- <<IS-END>> ---

                
	}



	public static final void getLastTestRunIdForTestId (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(getLastTestRunIdForTestId)>> ---
		// @sigtype java 3.5
		// [i] field:0:required testId
		// [o] field:0:required testRunId
		IDataMap pMap = new IDataMap(pipeline);
		String testId = pMap.getAsString("testId");
		
		// log("testId: " + testId);
		if (testId != null) {
			String testRunId = testRunIdStore.get(testId);
			// log("testRunId " + testRunId);
			pMap.put("testRunId", testRunId);
		} else {
			// log("not found");
		}
		
		// --- <<IS-END>> ---

                
	}



	public static final void registerInvokeChainProcessorSynchronized (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(registerInvokeChainProcessorSynchronized)>> ---
		// @sigtype java 3.5
		// [o] field:0:required result
		changeInvokeChainProcessorSynchronized(pipeline, true);
		// --- <<IS-END>> ---

                
	}



	public static final void storeTestRunId (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(storeTestRunId)>> ---
		// @sigtype java 3.5
		// [i] field:0:required testId
		// [i] field:0:required testRunId
		IDataMap pMap = new IDataMap(pipeline);
		String testId = pMap.getAsString("testId");
		String testRunId = pMap.getAsString("testRunId");
		
		if (testId != null) {
			testRunIdStore.put(testId, testRunId);
		}
		
		// --- <<IS-END>> ---

                
	}



	public static final void unregisterInvokeChainProcessorSynchronized (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(unregisterInvokeChainProcessorSynchronized)>> ---
		// @sigtype java 3.5
		// [o] field:0:required result
		changeInvokeChainProcessorSynchronized(pipeline, false);
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static ConcurrentHashMap<String, String> testRunIdStore = new ConcurrentHashMap<String, String>();
	
	private synchronized static final void changeInvokeChainProcessorSynchronized(IData pipeline, boolean register) {
		IDataMap pipeMap = new IDataMap(pipeline);
		if (register) {
			try {
				IData svcResult = Service.doInvoke("wx.interceptor.pub.config", "registerInvokeChainProcessor",
						(IData) null);
				IDataMap resultMap = new IDataMap(svcResult);
				String result = resultMap.getAsString("result");
				pipeMap.put("result", result);
				// log("SITCL: registerInvokeChainProcessor" + result);
			} catch (Exception e) {
				IDataMap map = new IDataMap(pipeline);
				map.put("result", e.getMessage());
			}
		} else {
			try {
				IData runningOutput = Service.doInvoke("wx.integrationTest.client.impl.util",
						"isInterceptorRunningForIntegrationTest", (IData) null);
				Service.doInvoke("pub.flow", "tracePipeline", runningOutput);
				IDataCursor cursor = runningOutput.getCursor();
				String isInterceptorRunningforIntegrationTest = IDataUtil.getString(cursor,
						"isInterceptorRunningForIntegrationTest");// runningOutputMap.getAsString("isInterceptorRunningforIntegrationTest");
				// log("cursor: " + );
				// log("SITCL: isInterceptorRunningforIntegrationTest " +
				// isInterceptorRunningforIntegrationTest);
				if ("false".equals(isInterceptorRunningforIntegrationTest)) {
					IData svcResult = Service.doInvoke("wx.interceptor.pub.config", "unregisterInvokeChainProcessor",
							(IData) null);
					IDataMap resultMap = new IDataMap(svcResult);
					String result = resultMap.getAsString("result");
					// log("SITCL: unregisterInvokeChainProcessor" + result);
					pipeMap.put("result", result);
				} else {
					pipeMap.put("result", "InvokeChainProcessor not unregistered because of running tests");
					// log("SITCL: InvokeChainProcessor not unregistered because of running tests");
				}
			} catch (Exception e) {
				IDataMap map = new IDataMap(pipeline);
				map.put("result", e.getMessage());
			}
	
		}
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

