/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smartitengineering.generator.engine.webservice.resource;

import com.smartitengineering.cms.api.content.Content;
import com.smartitengineering.generator.engine.webservice.domain.ReportConfig;
import com.smartitengineering.generator.engine.service.factory.Services;
import com.smartitengineering.generator.engine.webservice.adapter.ReportConfigAdapterHelper;
import com.smartitengineering.util.bean.adapter.GenericAdapterImpl;
import com.smartitengineering.util.rest.atom.server.AbstractResource;
import com.sun.jersey.api.view.Viewable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilderException;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author saumitra
 */
@Path("/reportconfigs/id/{id}")
public class ReportConfigResource extends AbstractResource{

  @PathParam("id")
  private String id;
  @Context
  private HttpServletRequest servletRequest;
  private GenericAdapterImpl<ReportConfig, com.smartitengineering.generator.engine.domain.ReportConfig> adapter;
  private com.smartitengineering.generator.engine.domain.ReportConfig persistentReportConfig;
  private static final Method CONFIG_CONTENT;


  static{
    try {
      CONFIG_CONTENT = ReportConfigResource.class.getMethod("getConfig");
    }
    catch (Exception ex) {
      throw new InstantiationError();

    }
  }

  public ReportConfigResource(@PathParam("id") String id) {
    persistentReportConfig = Services.getInstance().getReportConfigService().getById(id);
    adapter = new GenericAdapterImpl<ReportConfig, com.smartitengineering.generator.engine.domain.ReportConfig>();
    adapter.setHelper(new ReportConfigAdapterHelper());
  }



  @GET
  @Produces(MediaType.APPLICATION_ATOM_XML)
  public Response get() {
    ResponseBuilder responseBuilder = Response.status(Status.OK);
    if (persistentReportConfig == null) {
      return Response.status(Status.NOT_FOUND).build();
    }
    Feed configFeed =  getConfigFeed();
    responseBuilder = Response.ok(configFeed);
    return responseBuilder.build();
  }

  private Feed getConfigFeed() throws UriBuilderException, IllegalArgumentException {

    Feed configFeed = getFeed(persistentReportConfig.getId(), new Date());
    configFeed.setTitle(persistentReportConfig.getName());

    // add a self link
    configFeed.addLink(getSelfLink());

    // add a edit link
    Link editLink = getAbderaFactory().newLink();
    editLink.setHref(getUriInfo().getRequestUri().toString());
    editLink.setRel(Link.REL_EDIT);
    editLink.setMimeType(MediaType.APPLICATION_JSON);
    configFeed.addLink(editLink);

    // add a alternate link
    Link altLink = getAbderaFactory().newLink();
    altLink.setHref(getRelativeURIBuilder().path(ReportConfigResource.class).path(CONFIG_CONTENT).build(persistentReportConfig.getId()).toASCIIString());
    altLink.setRel(Link.REL_ALTERNATE);
    altLink.setMimeType(MediaType.APPLICATION_JSON);
    configFeed.addLink(altLink);

    // add content link

    Collection<Content> contents = Services.getInstance().getReportConfigService().getReport(persistentReportConfig.getId());
    if(contents != null && !contents.isEmpty()){
      for (Content content : contents){
        Entry contentEntry = getAbderaFactory().newEntry();
        contentEntry.setId(content.getContentId().toString());


        Link contentLink = getAbderaFactory().newLink();
        contentLink.setHref(getRelativeURIBuilder().path(ReportConfigResource.class).build(content.getContentId().toString()).
            toString());
        contentLink.setRel(Link.REL_ALTERNATE);
        contentLink.setMimeType(content.getRepresentation(id).getMimeType());

        contentEntry.addLink(contentLink);
        configFeed.addEntry(contentEntry);
      }
    }

    return configFeed;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/content")
  public Response getConfig() {
    ResponseBuilder responseBuilder = Response.ok(adapter.convertInversely(persistentReportConfig));
    return responseBuilder.build();
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getHtml(){
    ResponseBuilder responseBuilder = Response.status(Status.OK);
    return responseBuilder.build();
  }
  @POST
  @Path("/delete")
  public Response deletePost(ReportConfig reportConfig){
    com.smartitengineering.generator.engine.domain.ReportConfig persistentReportConfig = adapter.convert(reportConfig);
    try{
      Services.getInstance().getReportConfigService().delete(persistentReportConfig);
    }
    catch(Exception ex){
      servletRequest.setAttribute("error", ex);
    }
    ResponseBuilder responseBuilder = Response.status(Status.OK);
    return responseBuilder.build();
  }
  @DELETE
  public Response delete (){
    Services.getInstance().getReportConfigService().delete(persistentReportConfig);
    ResponseBuilder responseBuilder = Response.status(Status.OK);
    return responseBuilder.build();
 
  }

  @PUT
  @Produces(MediaType.APPLICATION_ATOM_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(ReportConfig reportConfig) {

    com.smartitengineering.generator.engine.domain.ReportConfig persistentReportConfig = adapter.convert(reportConfig);

    ResponseBuilder responseBuilder = Response.status(Status.OK);
    try {
      basicUpdate(persistentReportConfig);
      responseBuilder = Response.ok(getConfigFeed());
    }
    catch (Exception ex) {
      responseBuilder = Response.status(Status.INTERNAL_SERVER_ERROR);
    }
    return responseBuilder.build();
  }

  @POST
  @Path("/update")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response updatePost(@HeaderParam("Content-type") String contentType, String message) {
    ResponseBuilder responseBuilder = Response.ok();

    if (StringUtils.isBlank(message)) {
      responseBuilder = Response.status(Status.BAD_REQUEST);
      responseBuilder.build();
    }

    try {
      //Will search for the first '=' if not found will take the whole string
      final int startIndex = 0;//message.indexOf("=") + 1;
      //Consider the first '=' as the start of a value point and take rest as value
      final String realMsg = message.substring(startIndex);
      //Decode the message to ignore the form encodings and make them human readable
      message = URLDecoder.decode(realMsg, "UTF-8");
    }
    catch (UnsupportedEncodingException ex) {
      System.out.println(ex);
    }

    return responseBuilder.build();
  }

  private void basicUpdate(com.smartitengineering.generator.engine.domain.ReportConfig persistantCoupon) {
    Services.getInstance().getReportConfigService().update(persistentReportConfig);
  }

  @Override
  protected String getAuthor() {
    return "Smart Generator";
  }

}
