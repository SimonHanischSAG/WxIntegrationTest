package com.softwareag.de.s.gitjendis.builder.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import com.github.jochenw.afw.core.io.TerminationRequest;
import com.github.jochenw.afw.core.props.DefaultInterpolator;
import com.github.jochenw.afw.core.props.Interpolator;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Sax;
import com.github.jochenw.afw.core.util.Streams;

public class TestSuiteRunner {
	public static class IsPkg {
		private final Path dir;
		private final String name;

		public IsPkg(Path pDir, String pName) {
			dir = pDir;
			name = pName;
		}

		public Path getDir() { return dir; }
		public String getName() { return name; }
	}

	public static class TestSuiteFile {
		private final Path packageDir;
		private final String testSuiteFile;

		public TestSuiteFile(Path pPackageDir, String pTestSuiteFile) {
			packageDir = pPackageDir;
			testSuiteFile = pTestSuiteFile;
		}

		public Path getPackageDir() {
			return packageDir;
		}
		public String getTestSuiteFile() {
			return testSuiteFile;
		}
	}

	private final Path wmHomeDir;

	public TestSuiteRunner(Path pWmHomeDir) {
		wmHomeDir = pWmHomeDir;
	}

	public Path run(Path pProjectDir, Predicate<String> pScopePackages, Predicate<String> pTestSuiteFileFilter,
			        Path pTestDir, URL pIsUrl, String pIsUser, String pIsPassword, boolean pCoverage) {
		final List<IsPkg> packages = findIsPackages(pProjectDir);
		final List<String> coverageScopePackages = packages.stream().map((p) -> p.name).filter(pScopePackages).collect(Collectors.toList());
		final List<TestSuiteFile> testSuiteFiles = new ArrayList<>();
		for (IsPkg isPkg : packages) {
			findTestSuiteFiles(isPkg, pTestSuiteFileFilter, testSuiteFiles::add);
		}
		if (testSuiteFiles.isEmpty()) {
			return null;
		} else {
			return run(testSuiteFiles, coverageScopePackages, pProjectDir, pTestDir,
					   pIsUrl, pIsUser, pIsPassword, pCoverage);
		}
	}

	protected List<IsPkg> findIsPackages(Path pProjectDir) {
		final List<IsPkg> packages = new ArrayList<>();
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException {
				final Path manifestFile = pDir.resolve("manifest.v3");
				if (Files.isRegularFile(manifestFile)) {
					packages.add(new IsPkg(pDir, pDir.getFileName().toString()));
					return FileVisitResult.SKIP_SUBTREE;
				} else {
					return FileVisitResult.CONTINUE;
				}
			}
		};
		try {
			java.nio.file.Files.walkFileTree(pProjectDir, fv);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return packages;
	}

	protected void findTestSuiteFiles(IsPkg pPackage,
			Predicate<String> pTestSuiteFileFilter, Consumer<TestSuiteFile> pConsumer) {
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final String fileName = pFile.getFileName().toString();
				if (pAttrs.isRegularFile()  &&  fileName.endsWith(".xml")) {
					if (isTestSuiteFile(pFile, System.err::println)) {
						if (pTestSuiteFileFilter.test(fileName)) {
							log("Found test suite file " + pFile + ", accepted by filter.");
							final Path packageDir = pPackage.getDir();
							final String relativePath = packageDir.relativize(pFile).toString().replace('\\', '/');
							pConsumer.accept(new TestSuiteFile(packageDir, relativePath));
						} else {
							log("Ignoring test suite file " + pFile + ", due to filter.");
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};
		try {
			java.nio.file.Files.walkFileTree(pPackage.getDir(), fv);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	public Path run(@Nonnull List<TestSuiteFile> pTestSuiteFiles,
					@Nonnull List<String> pCoverageScopePackages, @Nonnull Path pProjectDir,
			        @Nonnull Path pTestDir, @Nonnull URL pIsUrl, @Nullable String pIsUser, @Nullable String pIsPassword, boolean pCoverage) {
		Holder<Path> reportFileHolder = new Holder<>();
		final String[] cmd = buildJavaCommand(pTestSuiteFiles, pCoverageScopePackages, pProjectDir, pTestDir, pIsUrl, pIsUser, pIsPassword, pCoverage,
				                              (p) -> reportFileHolder.set(p));
		execute(pProjectDir, pTestDir, cmd);
		return reportFileHolder.get();
	}

	protected Consumer<InputStream> getStdOutConsumer() {
		return (in) -> Streams.copy(in, System.out);
	}
	protected Consumer<InputStream> getStdErrConsumer() {
		return (in) -> Streams.copy(in, System.err);
	}
	protected IntConsumer getExitCodeHandler() {
		return (status) -> { if (status != 0) {
				throw new IllegalStateException("Invalid exit status: " + status);
			}
		};
	}

	protected void log(String pMsg) {
	}
	
	protected void execute(@Nonnull Path pProjectDir, @Nonnull Path pTestDir, final @Nonnull String[] cmd) {
		try {
			final Path junitPropertiesPath = pTestDir.resolve("junit.properties");
			java.nio.file.Files.createDirectories(pTestDir);
			java.nio.file.Files.createDirectories(getReportsDir(pTestDir));
			try (OutputStream out = java.nio.file.Files.newOutputStream(junitPropertiesPath)) {
				new Properties().store(out, null);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		new Executor().run(pProjectDir, cmd, null, getStdOutConsumer(), getStdErrConsumer(), null);
	}

	protected @Nonnull String[] buildJavaCommand(@Nonnull List<TestSuiteFile> pTestSuiteFiles, @Nonnull List<String> pCoverageScopePackages,
			                                    @Nonnull Path pProjectDir, @Nonnull Path pTestDir, @Nonnull URL pIsUrl,
			                                    @Nullable String pIsUser, @Nullable String pIsPassword, boolean pCoverage,
			                                    @Nullable Consumer<Path> pReportFileConsumer) {
		final List<String> cmd = new ArrayList<>();
		cmd.add(findJavaExe().toAbsolutePath().toString());
		cmd.add("-DwebMethods.integrationServer.name=" + pIsUrl.getHost());
		cmd.add("-DwebMethods.integrationServer.port=" + pIsUrl.getPort());
		if (pIsUser != null) {
			cmd.add("-DwebMethods.integrationServer.userid=" + pIsUser);
		}
		if (pIsPassword != null) {
			cmd.add("-DwebMethods.integrationServer.passsword=" + pIsPassword);
		}
		cmd.add("-DwebMethods.integrationServer.ssl=" + ("https".equals(pIsUrl.getProtocol())));
		cmd.add("-DwebMethods.test.setup.location=" + getLocationString(pTestSuiteFiles));
		cmd.add("-DwebMethods.test.setup.profile.mode=" + (pCoverage ? "COVERAGE" : "NONE"));
		if (pCoverage) {
			cmd.add("-DwebMethods.test.scope.packages=" + getPackagesString(pCoverageScopePackages));
		}
		final Path reportsDir = getReportsDir(pTestDir);
		cmd.add("-DwebMethods.test.profile.result.location=" + reportsDir);
		cmd.add("-DwebMethods.test.setup.external.classpath.layout=" + getClasspathLayout());
		cmd.add("-cp");
		cmd.add(String.join(File.pathSeparator, getClassPath(pProjectDir)));
		final Path reportFile = reportsDir.resolve("TEST-com.softwareag.utf.runner.UTFSuiteCompositeRunner$init.xml");
		if (pReportFileConsumer != null) {
			pReportFileConsumer.accept(reportFile);
		}
		final String junitArgs = "org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner"
				+ " com.softwareag.utf.runner.UTFSuiteCompositeRunner$init"
				+ " skipNonTests=false"
				+ " filtertrace=true"
				+ " haltOnError=false"
				+ " haltOnFailure=false"
				+ " formatter=org.apache.tools.ant.taskdefs.optional.junit.SummaryJUnitResultFormatter"
				+ " showoutput=true"
				+ " outputtoformatters=true"
				+ " logfailedtests=true"
				+ " threadid=0"
				+ " logtestlistenerevents=false"
				+ " formatter=org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter,${reportsDir}/TEST-com.softwareag.utf.runner.UTFSuiteCompositeRunner$init.txt"
				+ " formatter=com.softwareag.utf.runner.XMLJUnitResultFormatter,${reportFile}"
				+ " crashfile=${testDir}/junitvmwatcher.properties"
				+ " propsfile=${testDir}/junit.properties";
		final Interpolator interpolator = new DefaultInterpolator((k) -> {
			if ("reportsDir".equals(k)) {
				return reportsDir.toAbsolutePath().toString();
			} else if ("reportFile".equals(k)) {
				return reportFile.toAbsolutePath().toString();
			} else if ("testDir".equals(k)) {
				return pTestDir.toAbsolutePath().toString();
			} else {
				throw new IllegalStateException("Invalid argument property: Expected reportsDir|testDir, got " + k);
			}
		});
		for (final StringTokenizer st = new StringTokenizer(junitArgs, " ");  st.hasMoreTokens();  ) {
			final String arg = interpolator.interpolate(st.nextToken().trim());
			cmd.add(arg);
		}
		return Objects.requireNonNull(cmd.toArray(new String[cmd.size()]));
	}

	private Path getReportsDir(Path pTestDir) {
		final Path p = pTestDir.resolve("reports");
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return p;
	}

	protected Path findJavaExe() {
		final Path javaDir = wmHomeDir.resolve("jvm/jvm/bin");
		if (Files.isDirectory(javaDir)) {
			List<String> javaCandidates = Arrays.asList("javaw.exe", "java.exe", "java");
			for (String s : javaCandidates) {
				final Path javaPath = javaDir.resolve(s);
				if (Files.isRegularFile(javaPath)  &&  java.nio.file.Files.isExecutable(javaPath)) {
					return javaPath;
				}
			}
			throw new IllegalStateException("Unable to locate either of the Java candidates ("
					                        + String.join(", ", javaCandidates) + ") in Java directory " + javaDir);
		} else {
			throw new IllegalStateException("Unable to locate subdirectory jvm/jvm/bin inside wM home directory: " + wmHomeDir);
		}
	}

	protected String getLocationString(List<TestSuiteFile> pTestSuiteFiles) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < pTestSuiteFiles.size();  i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(pTestSuiteFiles.get(i).getPackageDir().toAbsolutePath().toString());
			sb.append(';');
			sb.append(pTestSuiteFiles.get(i).testSuiteFile);
		}
		return sb.toString();
	}

	protected String getPackagesString(List<String> pPackages) {
		return String.join(",", pPackages);
	}

	protected String getClasspathLayout() {
		return "resources/test/classes,resources/java/classes,resources/test/jars,resources/java/jars,resources/jars";
	}

	protected List<String> getClassPath(Path pProjectDir) {
		final List<String> list = new ArrayList<>();
		final String jarFileList = "common/lib/testsuite/commons-jxpath-1.2.jar;common/lib/testsuite/hamcrest-core.jar;"
				+ "common/lib/testsuite/httpunit.jar;common/lib/testsuite/junit.jar;"
				+ "common/lib/testsuite/serviceInterceptor.jar;common/lib/testsuite/serviceMockClient.jar;"
				+ "common/lib/testsuite/wmjxpath.jar;common/lib/testsuite/xmlunit1.0.jar;"
				+ "common/lib/glassfish/gf.jackson-core-asl.jar;common/lib/glassfish/gf.jackson-jaxrs.jar;"
				+ "common/lib/glassfish/gf.jackson-mapper-asl.jar;common/lib/ext/jackson-annotations.jar;"
				+ "common/lib/ext/jackson-core.jar;common/lib/ext/jackson-databind.jar;"
				+ "common/lib/ext/jackson-dataformat-yaml.jar;common/lib/ext/jackson-module-jaxb-annotations.jar;"
				+ "common/lib/wm-isclient.jar;common/lib/ext/icu4j.jar;"
				+ "common/lib/ext/enttoolkit.jar;IntegrationServer/lib/wm-isserver.jar;"
				+ "common/lib/wm-g11nutils.jar;common/lib/glassfish/gf.javax.mail.jar;"
				+ "common/lib/ant/ant-contrib-1.0b3.jar;common/lib/ant/lib/ant-launcher.jar;"
				+ "common/lib/ant/lib/ant.jar;common/lib/ant/lib/ant-junit.jar;common/lib/ant/lib/ant-junit4.jar";
		for (StringTokenizer st = new StringTokenizer(jarFileList, ";");  st.hasMoreTokens();  ) {
			final Path p = findJarFile(st.nextToken().trim());
			final String p1;
			final String p2 = p.toAbsolutePath().toString();
			try {
				p1 = pProjectDir.relativize(p).toString();
			} catch (IllegalArgumentException e) {
				list.add(p2);
				continue;
			}
			if (p1.length() < p2.length()) {
				list.add(p1);
			} else {
				list.add(p2);
			}
		}
		return list;
	}

	protected Path findJarFile(String pJarFile) {
		final Path p1 = Paths.get(pJarFile);
		if (java.nio.file.Files.isRegularFile(p1)) {
			return p1;
		}
		final Path p2 = wmHomeDir.resolve(pJarFile);
		if (java.nio.file.Files.isRegularFile(p2)) {
			return p2;
		}
		throw new IllegalStateException("Unable to locate jar file: " + pJarFile +
				                        ", tried " + p1 + ", and " + p2);
	}

	public static boolean isTestSuiteFile(Path pFile, Consumer<String> pErrorLogger) {
		if (pFile.getFileName().toString().endsWith(".xml")) {
			final Holder<String> uriHolder = new Holder<String>();
			final Holder<String> localNameHolder = new Holder<String>();
			final ContentHandler ch = new XMLFilterImpl() {
				@Override
				public void startElement(String pUri, String pLocalName, String pQName, Attributes pAtts)
						throws SAXException {
					localNameHolder.set(pLocalName);
					uriHolder.set(pUri);
					throw new TerminationRequest();
				}
			};
			try {
				Sax.parse(pFile, ch);
			} catch (TerminationRequest tr) {
				final String uri = uriHolder.get();
				final String localName = localNameHolder.get();
				return (uri == null  ||  uri.length() == 0) && "webMethodsTestSuite".equals(localName);
			} catch (Throwable t) {
				pErrorLogger.accept("Failed to parse test suite file candidate " + pFile
									+ ": " + t.getClass().getName() + ", " + t.getMessage());
			}
		}
		return false;
	}
}
