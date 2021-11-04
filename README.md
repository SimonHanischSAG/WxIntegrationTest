# WxIntegrationTest
webMethods IntegrationServer packages in order to develop automated IntegrationTests

It is designed for usage together with the official packages WxConfig (or the free alternative https://github.com/SimonHanischSAG/WxConfigLight) and optionally together with the official packages WxLog or WxLog2. Furthermore it is designed to use together with WxResilience (https://github.com/SimonHanischSAG/WxResilience). Finally the usage of the official package WxInterceptor is not mandatory but recommended and necessary in order to bring out the full strength of the tool.

MANY THANKS TO LIDL AND SCHWARZ IT, who kindly allowed to provide the template for this package and make it public.

<h2>How to use (basic configuration)</h2>

<h3>Provide WxInterceptor</h3>
compare with above

<h3>Provide WxConfig or WxConfigLight</h3>
compare with above

<h3>Provide WxResilience</h3>
compare with above

<h4>Setup UM stuff</h4>

Use EnterpriseManager to access the UM referenced by DEFAULT_IS_JMS_CONNECTION:

<ul>
  <li>Create ConnectionFactory local_um</li>
  <li>Create the topics ClientToControllerTopic, ControllerToOneClientOfClusterTopic and ControllerToAllClientsOfClusterTopic (and everntually the queue DemoTestedAToDemoTestedBQueue)</li>
  <li></li>
  <li></li>
  <li></li>
</ul>

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
