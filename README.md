# WxIntegrationTest
webMethods IntegrationServer packages in order to develop automated IntegrationTests. It is implemented as normal IntegrationServer packages in Flow/Java and can therefore use all features of IntegrationServer.

It is reusing WmTestSuite in order to implement integration tests which are executed like unit tests. In behind there is some asynchronous communication between the testing IS and the tested IS (-clusters) in order to allow to run tests in a pseudo-synchronic way using WmTestSuite.

It is designed for usage together with the official packages WxConfig (or the free alternative https://github.com/SimonHanischSAG/WxConfigLight) and optionally together with the official packages WxLog or WxLog2. Furthermore it is designed to use together with the free package WxResilience (https://github.com/SimonHanischSAG/WxResilience). Finally the usage of the official package WxInterceptor is not mandatory but recommended and necessary in order to bring out the full strength of the tool.

MANY THANKS TO LIDL AND SCHWARZ IT, who kindly allowed to provide the template for this package and make it public.

<h1>Architecture</h1>

<ol>
  <li>Use your own package like WxIntegrationTestDemo to place your (WmTestSuite) tests. Deploy that package on your "testing IS node" (= the "controller")</li>
  <li>Reuse the services of WxIntegrationTestController to implement these tests. Deploy that package on your "testing IS node" (= the "controller")</li>
  <li>In behind WxIntegrationTestClient (and maybe WxInterceptor) is deployed and used on the tested IS clusters (= the clients) in order to run the tests directly there.</li>
</ol>

In behind all the communication between Controller and Client is handled by JMS messages. The Controller itself is not directly starting a tested service on the client, it will always send a command to WxIntegrationTestClient for doing that. That loose coupling gives you more flexibility e.g. if you switch the WxIntegrationTestController_IS_JMS_CONNECTION (compare with below) on your local development IS to the UM of another stage. Of course the "testing IS node" and the "tested IS cluster" can be physically one and the same server.

<h1>How to setup</h1>

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
  <li>Create ConnectionFactory local_um and check that DEFAULT_IS_JMS_CONNECTION is enabled</li>
  <li>Create the topics ClientToControllerTopic, ControllerToOneClientOfClusterTopic and ControllerToAllClientsOfClusterTopic (and everntually the queues internal/SystemBTransferInboundQueue, external/SystemBInboundQueue if you want to run the demo tests)</li>
</ul>

<h4>Create JMS connection for WxIntegrationTestController</h4>

Create a JMS connection alias WxIntegrationTestController_IS_JMS_CONNECTION as an exact copy of DEFAULT_IS_JMS_CONNECTION. This additional connection can be used for staging (using your IS as a controller of another stage).

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
<h3>Run demo tests via WmTestSuite</h3>

Open WxIntegrationTestDemo\resources\test\integrationTests-DEV.xml in Designer and run the "Run Suite". In order to run every test successfully you have to configure the ACL Anonymous at wx.demo.systemA.pub.ws.resourceX:_get and wx.demo.systemA.pub.ws.resourceX:_post

<h3>Run demo tests via Designer</h3>

You can execute a test manually by running the related Flow service with a special input. That is also the preferred way how to develop a new integration test.

<h3>Run demo tests via REST</h3>

There will be a REST endpoint in WxIntegrationTestController in future in order to run all WmTestSuite tests of a package with a simple HTTP-GET.

<h1>How to use</h1>

<h2>Common stuff</h2>

Consider that you should use a unique testId (starting from 1000 when using together with WxInterceptor). Furthermore every run will generate a testRunId. This will help you to understand the internal communication in the logs:

2021-11-24 13:47:22 MEZ [ISP.0090.0004I] (tid=2346) WxIntegrationTestController -- Started test with testId 1005 and testRunId fe5c29b6-9e3b-4de3-9d3d-f49b276941eb
2021-11-24 13:47:22 MEZ [ISP.0090.0004I] (tid=2346) WxIntegrationTestController -- |testId:1005|testRunId:fe5c29b6-9e3b-4de3-9d3d-f49b276941eb| Send to client and wait for message
...
2021-11-24 13:47:23 MEZ [ISP.0090.0004I] (tid=324) WxIntegrationTestController -- WxIntegrationTestClient_to_WxIntegrationTestController |testId:1005|testRunId:fe5c29b6-9e3b-4de3-9d3d-f49b276941eb|clientHostname:sag-xxxxxxx.fritz.box| Processing finished for type:interceptionResponse|uuid:52355cb3-349a-4a4e-94bd-7ddd6128ee2d|service:wx.integrationTest.controller.impl:handleClientMessage|status:SUCCESS|duration:0.003
2021-11-24 13:47:24 MEZ [ISP.0090.0004I] (tid=2346) WxIntegrationTestController -- |testId:1005|testRunId:fe5c29b6-9e3b-4de3-9d3d-f49b276941eb| Received response for message
2021-11-24 13:47:25 MEZ [ISP.0090.0004I] (tid=2346) WxIntegrationTestController -- Ended test with testId 1005 and testRunId fe5c29b6-9e3b-4de3-9d3d-f49b276941eb


<h2>Packages</h2>

<h3>WxIntegrationTestClient</h3>

Normally as user you do not have to take a look on this package. But there are some helper service which can be usefull for development:

<h4>Helper services</h4>

<ul>
  <li>wx.integrationTest.client.impl.util:checkYourFilterExpression, wx.integrationTest.client.impl.util:checkFilterExpression, wx.integrationTest.client.impl.util:checkFilterExpressionDemo: These helper services shall help you to use set the correct inputs for registerInterceptorRemote related to WxInterceptor and its filter expression</li>
  <li>wx.integrationTest.client.impl.util:isInterceptorRunningForIntegrationTest, 
wx.integrationTest.client.impl.util:isWxInterceptorRegisteredInInvokeChain: If WxInterceptor is used it is added to the "invoke chain" of the IS and your Interceptor is stored in WxInterceptor. After the test both things a unregistered again. In order to check that manually you can use these helper services.</li>
</ul>

  
<h3>WxIntegrationTestController</h3>

<h4>pub/components: Components for building integration tests</h4>

<h5>Generic components</h5>

<ul>
  <li>wx.integrationTest.controller.pub.components:prepareGenericTest: Use that service at the beginning of each test which is <b>not</b> started by a JMS message</li>
  <li>wx.integrationTest.controller.pub.components:prepareJMSMessageTest: Use that service at the beginning of each test which is started by a JMS message</li>
  <li>wx.integrationTest.controller.pub.components:endTest: Use that service at the end of each test. It will care for the right structure on the pipeline</li>
</ul>

<h5>Specific components</h5>

All these components are designed for doing actions by the client servers/clusters and on the client servers/clusters executed by the WxIntegrationTestClient package. For that they have an input "requestTarget" which can be used to point at the desired IS cluster. If you have only one IS cluster you can ignore that field.

<ul>
  <li>wx.integrationTest.controller.pub.components:pingClients: A simple check if all clients of your tested cluster are responding</li>
  <li>wx.integrationTest.controller.pub.components:callHttpRemote: Like running pub.client:http direclty from the client</li>
  <li>wx.integrationTest.controller.pub.components:callWsdConsumerRemote: Consider that a proper WSD consumer must be on the related client server/cluster</li>
  <li>wx.integrationTest.controller.pub.components:invokeServiceRemote: Use for invoking any service on the client. Consider that the handling of the pipeline could by tricky</li>
  <li>wx.integrationTest.controller.pub.components:registerInterceptorRemote: Register an interception using WxInterceptor. Consider that this should be done before sending the test data for which WxInterceptor shall waiting for. You have to set a testId and a service which shall be intercepted. Consider that this will filter out the next execution of that service (with no further processing). Therefore you can and should use a filterExpression on the pipeline in order to filter out other data than expected.</li>
  <li>wx.integrationTest.controller.pub.components:pollForInterception: Has to be used together with registerInterceptorRemote. The test will wait till there is an response about an interception on the Client received by the Controller in behind.</li>
  <li>wx.integrationTest.controller.pub.components:sendJMSMessageRemote: Like running pub.jms:send directly from the client</li>
</ul>

<h4>pub/standardtests: Predefined integration tests</h4>

There are some predefined standard tests which can be used for developing integration tests without creating new Flow code:

<ul>
  <li>wx.integrationTest.controller.pub.standardtests:testCallHttp: Call a http provider and check the response</li>
  <li>wx.integrationTest.controller.pub.standardtests:testInvokeService: Call a service and check the output</li>
  <li>wx.integrationTest.controller.pub.standardtests:testJmsToInterceptor: Send a JMS message to a destination and wait that a specific service is invoked</li>
  <li>wx.integrationTest.controller.pub.standardtests:testPingClients: Ping some clients</li>
</ul>
