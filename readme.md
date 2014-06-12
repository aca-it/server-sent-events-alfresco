# Server Sent Events for Alfresco #

The server sent events JAR will allow you to incorporate SSE into your Alfresco modules.

To run the unit and integration tests, execute the following Maven command:

	$ mvn clean verify -Pamp-to-war,automated-it -Dpackaging.type=amp

This will *not* generate the JAR file that you can use in your AMP. To generate the JAR artifact run the following command:

	$ mvn clean install -DskipTests

## Using SSE in your AMP ##

First of all, you need to make sure that the necessary configuration is available. Configure the following properties in your `alfresco-global.properties`:

	# Determines how long a queued messages needs to be cached. Messages are queued when they delivery fails.
	sse.queued.message.lifetime.seconds=1000
	# Determines how long an SSE connection needs to be open. Connections are automatically refreshed if the user stays connected.
    sse.session.lifetime.seconds=60

Now you'll need a webscript which will deliver the events to the client. The JAR file contains a class called `SSEWebScript`. Register this class
as a WebScript in your module under any URL that suits you.

    <bean id="webscript.SSEWebScript.get" class="be.aca.alfresco.sse.webscript.SSEWebScript" parent="webscript">
       <property name="sseConfig" ref="sseConfig" />
       <property name="authorityService" ref="AuthorityService" />
    </bean>

    <webscript>
		<family>ACA</family>
		<shortname>My SSE WebScript</shortname>
		<url>/events</url>

		<format default="text">argument</format>
		<authentication>user</authentication>
	</webscript>

### Send events ###

Now that the WebScript is in place, you can start sending events in your module. Every event will be channeled to the correct recipient through
the SSE WebScript.
Inject the `be.aca.alfresco.sse.clientEventService` bean into your class and use that to send any message you would like:

	Message message = new Message((String) nodeService.getProperty(childAssociationRef.getChildRef(), ContentModel.PROP_NAME));
    clientEventService.sendMessageToUser("admin", message);

## Using SSE in your client ##

You can now use the WebScript URL in your client to receive server sent events. An example in JavaScript:

	var source = new EventSource('/alfresco/s/events');

	source.onopen = function () {
		console.log("open")
	};

	source.onerror = function () {
		console.log(source.readyState)
	};

	source.onmessage = function (event) {
		console.log("Message: " + event.data)
	};

	source.addEventListener("delete", function(e){
		console.log("Delete event: " + e)
	});

**Note:**

	Because server-sent events operate over HTTP, they can be used with standard web servers. However, because server-sent events maintain an open connection, they are not particularly well suited to some web servers. Apache, for example, delegates an operating system thread to each request. On a busy machine, server-sent events can quickly consume the pool of available threads.