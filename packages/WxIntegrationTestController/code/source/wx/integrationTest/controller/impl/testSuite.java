package wx.integrationTest.controller.impl;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import com.wm.app.b2b.server.Server;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import com.softwareag.de.s.gitjendis.builder.utils.TestSuiteRunner;
import com.softwareag.util.IDataMap;
// --- <<IS-END-IMPORTS>> ---

public final class testSuite

{
	// ---( internal utility methods )---

	final static testSuite _instance = new testSuite();

	static testSuite _newInstance() { return new testSuite(); }

	static testSuite _cast(Object o) { return (testSuite)o; }

	// ---( server methods )---




	public static final void executeTestSuite (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(executeTestSuite)>> ---
		// @sigtype java 3.5
		// [i] field:0:required package
		// [i] field:0:required isUrl
		// [i] field:0:required isUser
		// [i] field:0:required isPassword
		// [i] field:0:required coverage {"false","true"}
		final IDataMap map = new IDataMap(pipeline);
		final Function<String,String> paramGetter = (p) -> {
			final String v = map.getAsString(p);
			if (v == null) {
				throw new NullPointerException("Missing parameter: " + p);
			}
			if (v.length() == 0) {
				throw new IllegalArgumentException("Empty parameter: " + p);
			}
			return v;
		};
		final String packageName = paramGetter.apply("package");
		final Path packageDir = Paths.get("./packages/" + packageName);
		if (!Files.isDirectory(packageDir)) {
			throw new IllegalArgumentException("Invalid value for parameter package: "
					+ packageName + " (Package directory not found: " + packageDir + ")");
		}
		final String isUrlStr = paramGetter.apply("isUrl");
		final URL isUrl;
		try {
			isUrl = new URL(isUrlStr);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Invalid value for parameter isUrl: "
					+ " Expected valid URL, got " + isUrlStr);
		}
		final String isUser = paramGetter.apply("isUser");
		final String isPassword = paramGetter.apply("isPassword");
		final boolean coverage = map.getAsBoolean("coverage", Boolean.FALSE).booleanValue();
		final Path tempDir = Paths.get("./temp/packages/tests");
		try {
			Files.createDirectories(tempDir);
			final Path testDir = Files.createTempDirectory(tempDir, "ts");
			final Path instanceDir = Server.getHomeDir().toPath();
			final Path instancesDir = instanceDir.getParent();
			final Path isDir = instancesDir.getParent();
			final Path wmHomeDir = isDir.getParent();
			new TestSuiteRunner(wmHomeDir).run(packageDir, (s) -> true, testDir,
												isUrl, isUser, isPassword, coverage);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
		// --- <<IS-END>> ---

                
	}
}

