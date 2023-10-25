package io.antmedia.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;

import io.antmedia.plugin.EmotizePlugin;

@Component
@Path("/emotize-plugin")
public class RestService {

	@Context
	protected ServletContext servletContext;
	Gson gson = new Gson();

	/*
	 * Start generating transcriptions with given id
	 *
	 * @PathParam id: media push streamId
	 */
	@POST
	@Path("/{id}/start")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response start(@Context UriInfo uriInfo, @PathParam("id") String id) {
		if (uriInfo == null) {
			return Response.status(Status.BAD_REQUEST).entity("").build();
		}

		String host = uriInfo.getBaseUri().getHost();
		EmotizePlugin app = getPluginApp();

		app.start(id, host);

		return Response.status(Status.OK).entity("").build();
	}

	@GET
	@Path("/stats")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getStats() {
		EmotizePlugin app = getPluginApp();
		return app.getStats();
	}

	private EmotizePlugin getPluginApp() {
		ApplicationContext appCtx = (ApplicationContext) servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

		return (EmotizePlugin) appCtx.getBean("plugin.emotizeplugin");
	}
}
