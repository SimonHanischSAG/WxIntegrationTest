# WxIntegrationTest
webMethods IntegrationServer packages in order to develop automated IntegrationTests.

It is reusing WmTestSuite in order to implement integration tests which are executed like unit tests. In behind there is some asynchronous communication between the testing IS and the tested IS (-clusters) in order to allow to run tests in a pseudo-synchronic way using WmTestSuite.

It is designed for usage together with the official packages WxConfig (or the free alternative https://github.com/SimonHanischSAG/WxConfigLight) and optionally together with the official packages WxLog or WxLog2. Furthermore it is designed to use together with WxResilience (https://github.com/SimonHanischSAG/WxResilience). Finally the usage of the official package WxInterceptor is not mandatory but recommended and necessary in order to bring out the full strength of the tool.

MANY THANKS TO LIDL AND SCHWARZ IT, who kindly allowed to provide the template for this package and make it public.

<h1>Architecture</h1>

<ol>
  <li>Use your own package like WxIntegrationTestDemo to place your (WmTestSuite) tests. Deploy that package on your "testing IS node" (= the "controller")</li>
  <li>Reuse the services of WxIntegrationTestController to implement these tests. Deploy that package on your "testing IS node" (= the "controller")</li>
  <li>In behind WxIntegrationTestClient (and maybe WxInterceptor) is deployed and used on the tested IS clusters (= the clients) in order to run the tests directly there.</li>
</ol>

Of course the "testing IS node" and the "tested IS cluster" can be one and the same server.

<h1>How to use</h1>

<h2>Installation</h2>

<h3>Provide WxInterceptor</h3>
compare with above

<h3>Provide WxConfig or WxConfigLight</h3>
compare with above

<h3>Provide WxResilience</h3>
compare with above

<h3>Setup UM stuff</h3>

Use EnterpriseManager to access the UM referenced by DEFAULT_IS_JMS_CONNECTION:

<ul>
  <li>Create ConnectionFactory local_um</li>
  <li>Create the topics ClientToControllerTopic, ControllerToOneClientOfClusterTopic and ControllerToAllClientsOfClusterTopic (and everntually the queue DemoTestedAToDemoTestedBQueue)</li>
</ul>

<h4>Create JMS connection for WxIntegrationTestController</h4>

Create a JMS connection alias WxIntegrationTestController_IS_JMS_CONNECTION as an exact copy of DEFAULT_IS_JMS_CONNECTION. This is additional connection can be used for staging (using your IS as a controller of another stage).

<h3>Deploy/checkout WxIntegrationTest* packages</h3>

Check under releases for a proper release and deploy it. Otherwise you can check out the latest version from GIT and create a link like this:

mklink /J F:\\SoftwareAG\\IntegrationServer\\instances\\default\\packages\\WxIntegrationTestClient F:\\GIT-Repos\\WxResilience\\packages\\WxIntegrationTestClient
mklink /J F:\\SoftwareAG\\IntegrationServer\\instances\\default\\packages\\WxIntegrationTestController F:\\GIT-Repos\\WxResilience\\packages\\WxIntegrationTestController
mklink /J F:\\SoftwareAG\\IntegrationServer\\instances\\default\\packages\\WxIntegrationTestDoc F:\\GIT-Repos\\WxResilience\\packages\\WxIntegrationTestDoc

...

<h4>Build & Reload</h4>

If you checkout the sources from GitHub you have to compile the source, e.g. with:

C:\SoftwareAG\IntegrationServer\instances\default\bin\jcode.bat makeall WxIntegrationTestClient
C:\SoftwareAG\IntegrationServer\instances\default\bin\jcode.bat makeall WxIntegrationTestController
C:\SoftwareAG\IntegrationServer\instances\default\bin\jcode.bat makeall WxIntegrationTestDoc

Reload the packages

<h4>Initialize in case of WxConfigLight</h4>
If you are using WxConfigLight you have to run http://localhost:5555/invoke/wx.config.admin:replaceVariablesWithGlobalFile?wxConfigPkgName=WxIntegrationTestClient in order to load the keys of the package into the necessary Global Variables. WxConfig will do that automatically.

<h2>Execution</h2>
<h3>Run demo tests</h3>

Open WxIntegrationTestDemo\resources\test\integrationTests-DEV.xml in Designer and run the "Run Suite". In order to run every test successfully you have to configure the ACL Anonymous at wx.integrationTest.demoTestedA.pub.ws.resourceX:_get.
