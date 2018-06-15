# rockstar-ticketing
This is the code for the Adobe IO to Target sample app shown at Adobe Summit and Immerse 2018.
It is meant for POC and demo purposes only and is still a little rough.

##Installation
First set your Adobe IO Integration properties under 

`apps/rockstar-ticketing/config/com.aem.rockstar.ticketing.core.services.impl.AdobeIoServiceImpl.xml`

and your Youtube API Key under

`ui.ng/src/environments/environment.ts`

Make sure you have an AEM 4 instance running and from the root directory run 

`mvn clean install -P autoInstallPackage`